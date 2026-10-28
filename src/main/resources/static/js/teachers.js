(function () {
  const user = Shell.init({ allowedRoles: ["ADMIN", "HEADMASTER"], activeKey: "teachers", title: "Teachers" });
  if (!user) return;

  const canManage = user.role === "ADMIN";
  if (canManage) Utils.qs("#add-teacher-btn").classList.remove("hidden");

  const searchInput = Utils.qs("#search-input");
  const roleFilter = Utils.qs("#role-filter");
  let teachers = [];

  load();
  searchInput.addEventListener("input", Utils.debounce(render, 300));
  roleFilter.addEventListener("change", render);
  Utils.qs("#add-teacher-btn").addEventListener("click", () => openForm(null));

  async function load() {
    const region = Utils.qs("#teachers-region");
    Utils.setLoading(region, "Loading teachers...");
    try {
      teachers = await api.get("/api/teachers");
      render();
    } catch (err) {
      Utils.setError(region, err.message, load);
    }
  }

  function render() {
    const region = Utils.qs("#teachers-region");
    const term = searchInput.value.trim().toLowerCase();
    const role = roleFilter.value;
    const filtered = teachers.filter(
      (t) =>
        (!term || t.fullName.toLowerCase().includes(term) || t.username.toLowerCase().includes(term)) &&
        (!role || t.role === role)
    );
    if (!filtered.length) {
      Utils.setEmpty(region, "No teachers found.");
      return;
    }
    region.innerHTML = `
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr><th>Name</th><th>Username</th><th>Role</th><th>Phone</th><th>Status</th><th></th></tr></thead>
          <tbody>
            ${filtered
              .map(
                (t) => `
              <tr>
                <td>${Utils.escapeHtml(t.fullName)}</td>
                <td>${Utils.escapeHtml(t.username)}</td>
                <td><span class="badge badge-info">${t.role === "CLASS_TEACHER" ? "Class Teacher" : "Teacher"}</span></td>
                <td>${Utils.escapeHtml(t.phone || "-")}</td>
                <td>${t.active ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-danger">Inactive</span>'}</td>
                <td class="text-right">
                  ${
                    canManage
                      ? `<button class="btn btn-sm btn-secondary" data-action="edit" data-id="${t.id}">Edit</button>
                         ${t.active ? `<button class="btn btn-sm btn-outline-danger" data-action="deactivate" data-id="${t.id}">Deactivate</button>` : ""}
                         <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${t.id}">Remove</button>`
                      : ""
                  }
                </td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>`;
    Utils.qsa("[data-action]", region).forEach((btn) => {
      btn.addEventListener("click", () => handleAction(btn.dataset.action, btn.dataset.id));
    });
  }

  async function handleAction(action, id) {
    const teacher = teachers.find((t) => String(t.id) === String(id));
    if (action === "edit") { openForm(teacher); return; }
    if (action === "deactivate") {
      const ok = await Utils.confirmDialog({ title: "Deactivate teacher?", message: `${teacher.fullName} will no longer be able to log in.`, confirmText: "Deactivate" });
      if (!ok) return;
      try {
        await api.put(`/api/teachers/${id}/deactivate`);
        Utils.toast("Teacher deactivated.", "success");
        load();
      } catch (err) {
        Utils.errorToast(err);
      }
    } else if (action === "delete") {
      const ok = await Utils.confirmDialog({ title: "Remove teacher?", message: `This will permanently remove ${teacher.fullName}'s account.`, confirmText: "Remove" });
      if (!ok) return;
      try {
        await api.del(`/api/teachers/${id}`);
        Utils.toast("Teacher removed.", "success");
        load();
      } catch (err) {
        Utils.errorToast(err);
      }
    }
  }

  function openForm(teacher) {
    const isEdit = !!teacher;
    Utils.openModal(`
      <h3>${isEdit ? "Edit Teacher" : "Add Teacher"}</h3>
      <form id="teacher-form" novalidate>
        <div class="form-group">
          <label>Full Name <span class="req">*</span></label>
          <input type="text" name="fullName" required value="${isEdit ? Utils.escapeHtml(teacher.fullName) : ""}" />
          <div class="field-error" data-error-for="fullName"></div>
        </div>
        <div class="form-group">
          <label>Username <span class="req">*</span></label>
          <input type="text" name="username" required value="${isEdit ? Utils.escapeHtml(teacher.username) : ""}" />
          <div class="field-error" data-error-for="username"></div>
        </div>
        <div class="form-group">
          <label>Password <span class="req">*</span></label>
          <input type="password" name="password" required />
          <div class="text-faint" style="font-size:12px; margin-top:4px;">${isEdit ? "The backend requires a password on every update - enter the current or a new one." : "Minimum 6 characters recommended."}</div>
          <div class="field-error" data-error-for="password"></div>
        </div>
        <div class="form-group"><label>Phone</label><input type="tel" name="phone" value="${isEdit ? Utils.escapeHtml(teacher.phone || "") : ""}" /></div>
        <div class="form-group">
          <label>Email</label>
          <input type="email" name="email" value="${isEdit ? Utils.escapeHtml(teacher.email || "") : ""}" />
          <div class="field-error" data-error-for="email"></div>
        </div>
        <div id="form-alert"></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="form-cancel">Cancel</button>
          <button type="submit" class="btn btn-primary" id="form-save">${isEdit ? "Save Changes" : "Add Teacher"}</button>
        </div>
      </form>
    `);
    Utils.qs("#form-cancel").addEventListener("click", Utils.closeModal);
    Utils.qs("#teacher-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      Utils.clearFormErrors(form);
      const valid = Utils.validateForm(form, {
        fullName: [Utils.validators.required],
        username: [Utils.validators.required],
        password: [Utils.validators.required],
        email: [Utils.validators.email],
      });
      if (!valid) return;

      const payload = {
        fullName: form.fullName.value.trim(),
        username: form.username.value.trim(),
        password: form.password.value,
        phone: form.phone.value.trim() || null,
        email: form.email.value.trim() || null,
      };
      const btn = Utils.qs("#form-save");
      Utils.setButtonLoading(btn, true, "Saving...");
      try {
        if (isEdit) {
          await api.put(`/api/teachers/${teacher.id}`, payload);
          Utils.toast("Teacher updated successfully.", "success");
        } else {
          await api.post("/api/teachers", payload);
          Utils.toast("Teacher added successfully.", "success");
        }
        Utils.closeModal();
        load();
      } catch (err) {
        Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
        Utils.setButtonLoading(btn, false);
      }
    });
  }
})();
