/**
 * api.js - centralized communication with the Spring Boot backend.
 *
 * Every page should call api.get/post/put/del/upload/download instead of
 * using fetch() directly. This is the ONLY file that knows about the
 * backend's response envelope, auth header, and HTTP error handling.
 */

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

const api = (() => {
  function buildUrl(path) {
    if (/^https?:\/\//i.test(path)) return path;
    return CONFIG.apiBaseUrl + (path.startsWith("/") ? path : "/" + path);
  }

  function authHeaders(extra = {}) {
    const token = Auth.getToken();
    return token ? { ...extra, Authorization: "Bearer " + token } : extra;
  }

  async function handleResponse(res) {
    if (res.status === 204) return null;

    let body = null;
    const text = await res.text();
    if (text) {
      try { body = JSON.parse(text); } catch { body = null; }
    }

    if (res.status === 401) {
      Auth.logout(true);
      throw new ApiError("Your session has expired. Please log in again.", 401);
    }

    if (!res.ok) {
      const message =
        (body && body.message) ||
        friendlyMessageForStatus(res.status);
      throw new ApiError(message, res.status);
    }

    warnIfRecordsSkipped(res);

    if (body && typeof body.success === "boolean") {
      if (!body.success) {
        throw new ApiError(body.message || "Request failed", res.status);
      }
      return body.data;
    }
    return body;
  }

  // The backend skips (rather than fails on) any single record it cannot read,
  // e.g. a teacher whose user account was deleted directly in the database, and
  // reports how many via this header. Without this the only symptom used to be
  // a list or dropdown that is mysteriously missing entries.
  function warnIfRecordsSkipped(res) {
    const skipped = Number(res.headers && res.headers.get ? res.headers.get("X-Skipped-Records") : 0);
    if (skipped > 0 && typeof Utils !== "undefined") {
      Utils.toast(
        skipped + " record(s) could not be read and were left out of this list. " +
          "Check the server log, or see the Data check on the Admin dashboard.",
        "warning",
        9000
      );
    }
  }

  function friendlyMessageForStatus(status) {
    switch (status) {
      case 400: return "The information provided is invalid.";
      case 403: return "You do not have permission to perform this action.";
      case 404: return "The requested resource was not found.";
      case 409: return "This action conflicts with existing data.";
      case 413: return "The uploaded file is too large.";
      case 500: return "A server error occurred. Please try again later.";
      default: return "Something went wrong. Please try again.";
    }
  }

  async function request(method, path, { body, params } = {}) {
    let url = buildUrl(path);
    if (params) {
      const qs = new URLSearchParams();
      Object.entries(params).forEach(([k, v]) => {
        if (v !== undefined && v !== null && v !== "") qs.append(k, v);
      });
      const qsStr = qs.toString();
      if (qsStr) url += (url.includes("?") ? "&" : "?") + qsStr;
    }

    let res;
    try {
      res = await fetch(url, {
        method,
        headers: authHeaders(body ? { "Content-Type": "application/json" } : {}),
        body: body ? JSON.stringify(body) : undefined,
      });
    } catch (networkErr) {
      throw new ApiError("Unable to connect to the server. Check your connection or backend URL.", 0);
    }
    return handleResponse(res);
  }

  return {
    get: (path, params) => request("GET", path, { params }),
    post: (path, body) => request("POST", path, { body }),
    put: (path, body) => request("PUT", path, { body }),
    del: (path) => request("DELETE", path),

    /** Multipart form upload (documents, student photos, etc). */
    async upload(path, formData) {
      let res;
      try {
        res = await fetch(buildUrl(path), {
          method: "POST",
          headers: authHeaders(),
          body: formData,
        });
      } catch {
        throw new ApiError("Unable to connect to the server.", 0);
      }
      return handleResponse(res);
    },

    /** Downloads/streams a file (e.g. a student document) as a Blob. */
    async download(path) {
      let res;
      try {
        res = await fetch(buildUrl(path), { headers: authHeaders() });
      } catch {
        throw new ApiError("Unable to connect to the server.", 0);
      }
      if (res.status === 401) {
        Auth.logout(true);
        throw new ApiError("Your session has expired. Please log in again.", 401);
      }
      if (!res.ok) throw new ApiError(friendlyMessageForStatus(res.status), res.status);
      const blob = await res.blob();
      const filename = extractFilename(res.headers.get("Content-Disposition"));
      return { blob, filename };
    },
  };

  function extractFilename(disposition) {
    if (!disposition) return "download";
    const match = /filename="?([^"]+)"?/.exec(disposition);
    return match ? match[1] : "download";
  }
})();
