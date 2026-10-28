(function () {
  // Already logged in? Skip straight to the right dashboard.
  if (Auth.isAuthenticated()) {
    const user = Auth.getUser();
    location.href = Auth.dashboardPathForRole(user.role);
    return;
  }

  const params = new URLSearchParams(location.search);
  if (params.get("expired") === "1") {
    Utils.qs("#login-alert").innerHTML =
      '<div class="alert alert-warning">Your session expired. Please log in again.</div>';
  }

  const form = Utils.qs("#login-form");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    Utils.clearFormErrors(form);

    const valid = Utils.validateForm(form, {
      username: [Utils.validators.required],
      password: [Utils.validators.required],
    });
    if (!valid) return;

    const btn = Utils.qs("#login-btn");
    Utils.setButtonLoading(btn, true, "Signing in...");
    Utils.qs("#login-alert").innerHTML = "";

    try {
      const data = await Auth.login(form.username.value.trim(), form.password.value);
      location.href = Auth.dashboardPathForRole(data.role);
    } catch (err) {
      Utils.qs("#login-alert").innerHTML =
        `<div class="alert alert-error">${Utils.escapeHtml(err.message || "Invalid username or password.")}</div>`;
    } finally {
      Utils.setButtonLoading(btn, false);
    }
  });
})();
