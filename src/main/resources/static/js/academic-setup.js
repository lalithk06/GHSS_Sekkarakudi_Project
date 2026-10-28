(function () {
  const user = Shell.init({ allowedRoles: ["ADMIN"], activeKey: "academic", title: "Academic Setup" });
  if (!user) return;

  let standards = [];

  wireTabs();
  loadStandards();
  loadSections();
  loadSubjects();
  loadExams();

  Utils.qs("#add-std-btn").addEventListener("click", () => openStandardForm(null));
  Utils.qs("#add-sec-btn").addEventListener("click", () => openSectionForm(null));
  Utils.qs("#add-sub-btn").addEventListener("click", () => openSubjectForm(null));
  Utils.qs("#add-exam-btn").addEventListener("click", () => openExamForm());

  function wireTabs() {
    function activate(tabKey) {
      const btn = Utils.qs(`.tab-btn[data-tab="${tabKey}"]`);
      const panel = Utils.qs(`#tab-${tabKey}`);
      if (!btn || !panel) return;
      Utils.qsa(".tab-btn").forEach((b) => b.classList.remove("active"));
      Utils.qsa(".tab-panel").forEach((p) => p.classList.remove("active"));
      btn.classList.add("active");
      panel.classList.add("active");
    }
    Utils.qsa(".tab-btn").forEach((btn) => {
      btn.addEventListener("click", () => activate(btn.dataset.tab));
    });
    // Deep-link support: other pages (e.g. Assignments, when no subjects are
    // mapped to a section/group yet) link straight to academic-setup.html#sub
    // so the admin lands directly on the relevant tab instead of Standards.
    const hashTab = (window.location.hash || "").replace("#", "").trim();
    if (hashTab) activate(hashTab);
  }

  // ---------------- Standards ----------------
  async function loadStandards() {
    const region = Utils.qs("#std-table");
    Utils.setLoading(region, "Loading standards...");
    try {
      standards = await AcademicApi.getStandards(true);
      if (!standards.length) { Utils.setEmpty(region, "No standards created yet."); return; }
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Name</th><th>Description</th><th></th></tr></thead>
            <tbody>
              ${standards
                .map(
                  (s) => `<tr>
                  <td>${Utils.escapeHtml(s.name)}</td><td>${Utils.escapeHtml(s.description || "-")}</td>
                  <td class="text-right">
                    <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${s.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${s.id}">Delete</button>
                  </td></tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>`;
      Utils.qsa("[data-action]", region).forEach((btn) => {
        btn.addEventListener("click", () => {
          const s = standards.find((x) => String(x.id) === btn.dataset.id);
          if (btn.dataset.action === "edit") openStandardForm(s);
          else deleteItem("/api/standards", s.id, "standard", loadStandards);
        });
      });
    } catch (err) {
      Utils.setError(region, err.message, loadStandards);
    }
  }

  function openStandardForm(std) {
    const isEdit = !!std;
    Utils.openModal(`
      <h3>${isEdit ? "Edit" : "Add"} Standard</h3>
      <form id="std-form" novalidate>
        <div class="form-group"><label>Name <span class="req">*</span></label>
          <input type="text" name="name" required value="${isEdit ? Utils.escapeHtml(std.name) : ""}" placeholder="e.g. 10 or 12" />
        </div>
        <div class="form-group"><label>Description</label>
          <input type="text" name="description" value="${isEdit ? Utils.escapeHtml(std.description || "") : ""}" />
        </div>
        <div id="form-alert"></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary">Save</button>
        </div>
      </form>`);
    Utils.qs("#cancel-btn").addEventListener("click", Utils.closeModal);
    Utils.qs("#std-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      if (!Utils.validateForm(form, { name: [Utils.validators.required] })) return;
      const payload = { name: form.name.value.trim(), description: form.description.value.trim() || null };
      try {
        if (isEdit) await api.put(`/api/standards/${std.id}`, payload);
        else await api.post("/api/standards", payload);
        Utils.toast(`Standard ${isEdit ? "updated" : "added"} successfully.`, "success");
        Utils.closeModal();
        loadStandards();
      } catch (err) {
        Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
      }
    });
  }

  // ---------------- Sections / Groups ----------------
  async function loadSections() {
    const region = Utils.qs("#sec-table");
    Utils.setLoading(region, "Loading sections/groups...");
    try {
      const sections = await AcademicApi.getSections();
      if (!sections.length) { Utils.setEmpty(region, "No sections/groups created yet."); return; }
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Standard</th><th>Name</th><th>Status</th><th></th></tr></thead>
            <tbody>
              ${sections
                .map(
                  (sg) => `<tr>
                  <td>${Utils.escapeHtml(sg.standardName)}</td><td>${Utils.escapeHtml(sg.name)}</td>
                  <td>${sg.active ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-danger">Inactive</span>'}</td>
                  <td class="text-right">
                    <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${sg.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${sg.id}">Delete</button>
                  </td></tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>`;
      Utils.qsa("[data-action]", region).forEach((btn) => {
        btn.addEventListener("click", () => {
          const sg = sections.find((x) => String(x.id) === btn.dataset.id);
          if (btn.dataset.action === "edit") openSectionForm(sg);
          else deleteItem("/api/sections", sg.id, "section/group", loadSections);
        });
      });
    } catch (err) {
      Utils.setError(region, err.message, loadSections);
    }
  }

  async function openSectionForm(sg) {
    const isEdit = !!sg;
    if (!standards.length) standards = await AcademicApi.getStandards();
    Utils.openModal(`
      <h3>${isEdit ? "Edit" : "Add"} Section/Group</h3>
      <form id="sec-form" novalidate>
        <div class="form-group"><label>Standard <span class="req">*</span></label>
          <select name="standardId" required>${standards.map((s) => `<option value="${s.id}" ${isEdit && s.id === sg.standardId ? "selected" : ""}>${Utils.escapeHtml(s.name)}</option>`).join("")}</select>
        </div>
        <div class="form-group"><label>Name <span class="req">*</span></label>
          <input type="text" name="name" required value="${isEdit ? Utils.escapeHtml(sg.name) : ""}" placeholder="e.g. A or Bio-Maths" />
        </div>
        <div id="form-alert"></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary">Save</button>
        </div>
      </form>`);
    Utils.qs("#cancel-btn").addEventListener("click", Utils.closeModal);
    Utils.qs("#sec-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      if (!Utils.validateForm(form, { name: [Utils.validators.required], standardId: [Utils.validators.required] })) return;
      const payload = { standardId: Number(form.standardId.value), name: form.name.value.trim() };
      try {
        if (isEdit) await api.put(`/api/sections/${sg.id}`, payload);
        else await api.post("/api/sections", payload);
        Utils.toast(`Section/Group ${isEdit ? "updated" : "added"} successfully.`, "success");
        Utils.closeModal();
        loadSections();
      } catch (err) {
        Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
      }
    });
  }

  // ---------------- Subjects ----------------
  async function loadSubjects() {
    const region = Utils.qs("#sub-table");
    Utils.setLoading(region, "Loading subjects...");
    try {
      const subjects = await AcademicApi.getSubjects();
      if (!subjects.length) { Utils.setEmpty(region, "No subjects created yet."); return; }
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Standard</th><th>Subject</th><th>Section(s)/Group(s)</th><th></th></tr></thead>
            <tbody>
              ${subjects
                .map(
                  (s) => `<tr>
                  <td>${Utils.escapeHtml(s.standardName)}</td><td>${Utils.escapeHtml(s.name)}</td>
                  <td>${
                    (s.sectionGroupNames || []).length
                      ? s.sectionGroupNames.map((n) => `<span class="badge badge-primary">${Utils.escapeHtml(n)}</span>`).join(" ")
                      : '<span class="text-faint">Not assigned to any section/group yet</span>'
                  }</td>
                  <td class="text-right">
                    <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${s.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${s.id}">Delete</button>
                  </td></tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>`;
      Utils.qsa("[data-action]", region).forEach((btn) => {
        btn.addEventListener("click", () => {
          const s = subjects.find((x) => String(x.id) === btn.dataset.id);
          if (btn.dataset.action === "edit") openSubjectForm(s);
          else deleteItem("/api/subjects", s.id, "subject", loadSubjects);
        });
      });
    } catch (err) {
      Utils.setError(region, err.message, loadSubjects);
    }
  }

  async function openSubjectForm(subject) {
    const isEdit = !!subject;
    if (!standards.length) standards = await AcademicApi.getStandards();
    Utils.openModal(`
      <h3>${isEdit ? "Edit" : "Add"} Subject</h3>
      <form id="sub-form" novalidate>
        <div class="form-group"><label>Standard <span class="req">*</span></label>
          <select name="standardId" required>${standards.map((s) => `<option value="${s.id}" ${isEdit && s.id === subject.standardId ? "selected" : ""}>${Utils.escapeHtml(s.name)}</option>`).join("")}</select>
        </div>
        <div class="form-group"><label>Subject Name <span class="req">*</span></label>
          <input type="text" name="name" required value="${isEdit ? Utils.escapeHtml(subject.name) : ""}" />
        </div>
        <div class="form-group">
          <label>Section(s)/Group(s)</label>
          <p class="text-faint" style="font-size:12.5px; margin:2px 0 6px;">Which sections (10th) or groups (12th) actually offer this subject. e.g. "Commerce" would only be ticked under History for 12th.</p>
          <div id="sub-groups-list"><div class="state-block"><div class="spinner"></div>Loading...</div></div>
        </div>
        <div id="form-alert"></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary">Save</button>
        </div>
      </form>`);
    Utils.qs("#cancel-btn").addEventListener("click", Utils.closeModal);

    const standardSelect = Utils.qs('select[name="standardId"]');
    const selectedGroupIds = new Set((isEdit ? subject.sectionGroupIds : []) || []);

    async function loadGroupCheckboxes() {
      const listEl = Utils.qs("#sub-groups-list");
      listEl.innerHTML = `<div class="state-block"><div class="spinner"></div>Loading...</div>`;
      try {
        const groups = await AcademicApi.getSections(standardSelect.value);
        if (!groups.length) {
          listEl.innerHTML = `<p class="text-faint">No sections/groups exist for this standard yet - add one under Sections/Groups first.</p>`;
          return;
        }
        listEl.innerHTML = groups
          .map(
            (g) => `
          <label class="checkbox-row" style="display:flex; align-items:center; gap:8px; margin-bottom:6px;">
            <input type="checkbox" name="groupIds" value="${g.id}" ${selectedGroupIds.has(g.id) ? "checked" : ""} />
            <span>${Utils.escapeHtml(g.name)}</span>
          </label>`
          )
          .join("");
      } catch (err) {
        listEl.innerHTML = `<p class="text-faint">Could not load sections/groups.</p>`;
      }
    }
    standardSelect.addEventListener("change", loadGroupCheckboxes);
    await loadGroupCheckboxes();

    Utils.qs("#sub-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      if (!Utils.validateForm(form, { name: [Utils.validators.required], standardId: [Utils.validators.required] })) return;
      const standardId = Number(form.standardId.value);
      const name = form.name.value.trim();
      const checkedGroupIds = Utils.qsa('input[name="groupIds"]:checked', form).map((cb) => Number(cb.value));

      try {
        let subjectId;
        if (isEdit) {
          await api.put(`/api/subjects/${subject.id}`, { standardId, name });
          subjectId = subject.id;
        } else {
          try {
            const created = await api.post("/api/subjects", { standardId, name });
            subjectId = created.id;
          } catch (err) {
            // Subject already exists for this standard (e.g. "Economics" was
            // already created for another group) - reuse it instead of
            // failing, so the admin can still map it to the new group(s).
            if (err.status === 409) {
              const existing = await AcademicApi.getSubjects(standardId);
              const match = existing.find((s) => s.name.trim().toLowerCase() === name.toLowerCase());
              if (!match) throw err;
              subjectId = match.id;
              Utils.toast(`"${name}" already exists for this standard - updating its section/group list instead.`, "info");
            } else {
              throw err;
            }
          }
        }

        // Reconcile group mappings: map newly-checked groups, unmap
        // previously-mapped groups that got unchecked.
        const previousGroupIds = isEdit ? (subject.sectionGroupIds || []) : [];
        const toAdd = checkedGroupIds.filter((id) => !previousGroupIds.includes(id));
        const toRemove = previousGroupIds.filter((id) => !checkedGroupIds.includes(id));
        for (const gid of toAdd) await api.post(`/api/subjects/${subjectId}/groups/${gid}`, {});
        for (const gid of toRemove) await api.del(`/api/subjects/${subjectId}/groups/${gid}`);

        Utils.toast(`Subject ${isEdit ? "updated" : "added"} successfully.`, "success");
        Utils.closeModal();
        loadSubjects();
      } catch (err) {
        Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
      }
    });
  }

  // ---------------- Exams ----------------
  async function loadExams() {
    const region = Utils.qs("#exam-table");
    Utils.setLoading(region, "Loading exams...");
    try {
      const exams = await api.get("/api/exams");
      if (!exams.length) { Utils.setEmpty(region, "No exams created yet."); return; }
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Standard</th><th>Exam Name</th><th></th></tr></thead>
            <tbody>
              ${exams
                .map(
                  (e) => `<tr>
                  <td>${Utils.escapeHtml(e.standardName)}</td><td>${Utils.escapeHtml(e.name)}</td>
                  <td class="text-right"><button class="btn btn-sm btn-outline-danger" data-id="${e.id}">Delete</button></td>
                  </tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>`;
      Utils.qsa("button[data-id]", region).forEach((btn) => {
        btn.addEventListener("click", () => deleteItem("/api/exams", btn.dataset.id, "exam", loadExams));
      });
    } catch (err) {
      Utils.setError(region, err.message, loadExams);
    }
  }

  async function openExamForm() {
    if (!standards.length) standards = await AcademicApi.getStandards();
    Utils.openModal(`
      <h3>Add Exam</h3>
      <form id="exam-form" novalidate>
        <div class="form-group"><label>Standard <span class="req">*</span></label>
          <select name="standardId" required>${standards.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("")}</select>
        </div>
        <div class="form-group"><label>Exam Name <span class="req">*</span></label>
          <input type="text" name="name" required placeholder="e.g. Quarterly, Half-Yearly" />
        </div>
        <div id="form-alert"></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary">Save</button>
        </div>
      </form>`);
    Utils.qs("#cancel-btn").addEventListener("click", Utils.closeModal);
    Utils.qs("#exam-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      if (!Utils.validateForm(form, { name: [Utils.validators.required], standardId: [Utils.validators.required] })) return;
      try {
        await api.post("/api/exams", { standardId: Number(form.standardId.value), name: form.name.value.trim() });
        Utils.toast("Exam added successfully.", "success");
        Utils.closeModal();
        loadExams();
      } catch (err) {
        Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
      }
    });
  }

  // ---------------- Shared delete helper ----------------
  async function deleteItem(basePath, id, label, reload) {
    const ok = await Utils.confirmDialog({ title: `Delete ${label}?`, message: "This cannot be undone.", confirmText: "Delete" });
    if (!ok) return;
    try {
      await api.del(`${basePath}/${id}`);
      Utils.toast(`${label[0].toUpperCase() + label.slice(1)} deleted.`, "success");
      reload();
    } catch (err) {
      Utils.errorToast(err);
    }
  }
})();
