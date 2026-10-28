/**
 * utils.js - small reusable helpers shared across pages.
 */

const Utils = (() => {
  function qs(sel, root = document) { return root.querySelector(sel); }
  function qsa(sel, root = document) { return Array.from(root.querySelectorAll(sel)); }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
  }

  function ensureToastContainer() {
    let el = document.getElementById("toast-container");
    if (!el) {
      el = document.createElement("div");
      el.id = "toast-container";
      document.body.appendChild(el);
    }
    return el;
  }

  function toast(message, type = "info", duration = 3800) {
    const container = ensureToastContainer();
    const el = document.createElement("div");
    el.className = `toast toast-${type}`;
    el.textContent = message;
    container.appendChild(el);
    setTimeout(() => {
      el.style.opacity = "0";
      el.style.transition = "opacity 0.2s";
      setTimeout(() => el.remove(), 200);
    }, duration);
  }

  function errorToast(err) {
    toast(err && err.message ? err.message : "Something went wrong.", "error");
  }

  function ensureModalRoot() {
    let el = document.getElementById("modal-root");
    if (!el) {
      el = document.createElement("div");
      el.id = "modal-root";
      document.body.appendChild(el);
    }
    return el;
  }

  function closeModal() {
    const root = ensureModalRoot();
    root.innerHTML = "";
  }

  /** Simple confirmation dialog. Returns a Promise<boolean>. */
  function confirmDialog({ title = "Are you sure?", message = "", confirmText = "Confirm", danger = true } = {}) {
    return new Promise((resolve) => {
      const root = ensureModalRoot();
      root.innerHTML = `
        <div class="modal-backdrop" data-close>
          <div class="modal-box" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
            <h3 id="confirm-title">${escapeHtml(title)}</h3>
            <p>${escapeHtml(message)}</p>
            <div class="modal-actions">
              <button class="btn btn-secondary" data-action="cancel">Cancel</button>
              <button class="btn ${danger ? "btn-danger" : "btn-primary"}" data-action="confirm">${escapeHtml(confirmText)}</button>
            </div>
          </div>
        </div>`;
      root.querySelector('[data-action="cancel"]').onclick = () => { closeModal(); resolve(false); };
      root.querySelector('[data-action="confirm"]').onclick = () => { closeModal(); resolve(true); };
      root.querySelector(".modal-backdrop").addEventListener("click", (e) => {
        if (e.target.dataset.close !== undefined && e.target === e.currentTarget) { closeModal(); resolve(false); }
      });
    });
  }

  /** Renders arbitrary HTML into a modal box (for upload forms, etc). Returns the modal root element. */
  function openModal(innerHtml, { wide = false } = {}) {
    const root = ensureModalRoot();
    root.innerHTML = `
      <div class="modal-backdrop" data-close>
        <div class="modal-box ${wide ? "modal-wide" : ""}">${innerHtml}</div>
      </div>`;
    root.querySelector(".modal-backdrop").addEventListener("click", (e) => {
      if (e.target === e.currentTarget) closeModal();
    });
    return root;
  }

  function setLoading(container, message = "Loading...") {
    container.innerHTML = `<div class="state-block"><div class="spinner"></div><div>${escapeHtml(message)}</div></div>`;
  }

  function setEmpty(container, message = "No records found.", icon = "\u{1F4ED}") {
    container.innerHTML = `<div class="state-block"><div class="state-icon">${icon}</div><div>${escapeHtml(message)}</div></div>`;
  }

  function setError(container, message = "Something went wrong.", retryFn = null) {
    container.innerHTML = `
      <div class="state-block">
        <div class="state-icon">\u26A0\uFE0F</div>
        <div>${escapeHtml(message)}</div>
        ${retryFn ? '<button class="btn btn-secondary mt-10" id="state-retry-btn">Retry</button>' : ""}
      </div>`;
    if (retryFn) container.querySelector("#state-retry-btn").onclick = retryFn;
  }

  function formatDate(dateStr) {
    if (!dateStr) return "-";
    const d = new Date(dateStr);
    if (isNaN(d)) return dateStr;
    return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
  }

  function formatDateTime(dateStr) {
    if (!dateStr) return "-";
    const d = new Date(dateStr);
    if (isNaN(d)) return dateStr;
    return d.toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
  }

  function todayIso() {
    const d = new Date();
    return d.toISOString().slice(0, 10);
  }

  function initials(name) {
    if (!name) return "?";
    return name.trim().split(/\s+/).slice(0, 2).map((p) => p[0].toUpperCase()).join("");
  }

  function debounce(fn, wait = 350) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), wait); };
  }

  function setButtonLoading(btn, loading, loadingText = "Please wait...") {
    if (loading) {
      btn.dataset.originalText = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = `<span class="inline-spinner"></span> ${escapeHtml(loadingText)}`;
    } else {
      btn.disabled = false;
      if (btn.dataset.originalText) btn.innerHTML = btn.dataset.originalText;
    }
  }

  // ---- validation helpers ----
  const validators = {
    required: (v) => (v !== undefined && v !== null && String(v).trim() !== "") || "This field is required.",
    email: (v) => !v || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) || "Enter a valid email address.",
    phone: (v) => !v || /^[0-9+\-\s]{7,15}$/.test(v) || "Enter a valid phone number.",
    maxLen: (n) => (v) => !v || String(v).length <= n || `Must be at most ${n} characters.`,
    numberInRange: (min, max) => (v) => {
      if (v === "" || v === null || v === undefined) return true;
      const num = Number(v);
      if (isNaN(num)) return "Must be a number.";
      if (num < min || num > max) return `Must be between ${min} and ${max}.`;
      return true;
    },
  };

  /**
   * Validates a form given a rules map: { fieldName: [validatorFns] }.
   * Shows inline errors next to fields with [data-error-for="fieldName"].
   * Returns true if the whole form is valid.
   */
  function validateForm(form, rules) {
    let valid = true;
    Object.entries(rules).forEach(([field, fns]) => {
      const input = form.querySelector(`[name="${field}"]`);
      const errorEl = form.querySelector(`[data-error-for="${field}"]`);
      if (!input) return;
      let message = null;
      for (const fn of fns) {
        const result = fn(input.value);
        if (result !== true) { message = result; break; }
      }
      if (message) {
        valid = false;
        input.classList.add("input-error");
        if (errorEl) { errorEl.textContent = message; errorEl.classList.add("show"); }
      } else {
        input.classList.remove("input-error");
        if (errorEl) { errorEl.classList.remove("show"); errorEl.textContent = ""; }
      }
    });
    return valid;
  }

  function clearFormErrors(form) {
    qsa(".input-error", form).forEach((el) => el.classList.remove("input-error"));
    qsa(".field-error", form).forEach((el) => { el.classList.remove("show"); el.textContent = ""; });
  }

  return {
    qs, qsa, escapeHtml, toast, errorToast, confirmDialog, openModal, closeModal,
    setLoading, setEmpty, setError, formatDate, formatDateTime, todayIso, initials,
    debounce, setButtonLoading, validators, validateForm, clearFormErrors,
  };
})();
