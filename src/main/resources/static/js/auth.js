/**
 * auth.js - centralized authentication.
 *
 * Handles login, logout, token storage/retrieval, auth checks, and
 * redirecting to the correct dashboard per role. No other file should
 * touch localStorage for the token/user directly.
 */

const Auth = (() => {
  const TOKEN_KEY = "sms_token";
  const USER_KEY = "sms_user";

  const DASHBOARD_BY_ROLE = {
    HEADMASTER: "hm-dashboard.html",
    ADMIN: "admin-dashboard.html",
    CLASS_TEACHER: "class-teacher-dashboard.html",
    SUBJECT_TEACHER: "subject-teacher-dashboard.html",
  };

  const ROLE_LABELS = {
    HEADMASTER: "Headmaster",
    ADMIN: "Administrator",
    CLASS_TEACHER: "Class Teacher",
    SUBJECT_TEACHER: "Subject Teacher",
  };

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function isAuthenticated() {
    return !!getToken();
  }

  async function login(username, password) {
    const data = await api.post("/api/auth/login", { username, password });
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(
      USER_KEY,
      JSON.stringify({ id: data.userId, username: data.username, role: data.role })
    );
    return data;
  }

  function logout(sessionExpired = false) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    if (!location.pathname.endsWith("login.html")) {
      const target = "login.html" + (sessionExpired ? "?expired=1" : "");
      location.href = location.pathname.includes("/pages/") ? target : "pages/" + target;
    }
  }

  function dashboardPathForRole(role) {
    return DASHBOARD_BY_ROLE[role] || "login.html";
  }

  function roleLabel(role) {
    return ROLE_LABELS[role] || role;
  }

  /**
   * Call at the top of every protected page.
   * allowedRoles: array of role strings permitted to view this page.
   * Redirects to login if not authenticated, or to the user's own
   * dashboard if authenticated but not permitted here.
   */
  function requireAuth(allowedRoles) {
    if (!isAuthenticated()) {
      location.href = "login.html";
      return null;
    }
    const user = getUser();
    if (allowedRoles && allowedRoles.length && !allowedRoles.includes(user.role)) {
      location.href = dashboardPathForRole(user.role);
      return null;
    }
    return user;
  }

  return { getToken, getUser, isAuthenticated, login, logout, dashboardPathForRole, roleLabel, requireAuth };
})();
