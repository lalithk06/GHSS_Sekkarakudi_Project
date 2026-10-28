/**
 * Central place to configure the Spring Boot backend URL.
 *
 * This frontend is served directly by the Spring Boot app itself (from
 * src/main/resources/static), so requests are same-origin by default -
 * leaving this blank means "whatever host/port served this page".
 *
 * If you ever serve the frontend separately (e.g. a different static host)
 * point it at the backend explicitly, either by editing the line below or
 * at runtime from the browser console:
 *
 *   localStorage.setItem('sms_api_base_url', 'http://your-server:8080');
 */
const DEFAULT_API_BASE_URL = "";

const CONFIG = {
  get apiBaseUrl() {
    return localStorage.getItem("sms_api_base_url") || DEFAULT_API_BASE_URL;
  },
  set apiBaseUrl(url) {
    localStorage.setItem("sms_api_base_url", url.replace(/\/+$/, ""));
  },
};
