(function () {
  const user = Shell.init({ allowedRoles: ["ADMIN"], activeKey: "assignments", title: "Assignments" });
  if (!user) return;

  let teachers = [];

  wireTabs();

  function wireTabs() {
    Utils.qsa(".tab-btn").forEach((btn) => {
      btn.addEventListener("click", () => {
        Utils.qsa(".tab-btn").forEach((b) => b.classList.remove("active"));
        Utils.qsa(".tab-panel").forEach((p) => p.classList.remove("active"));
        btn.classList.add("active");
        Utils.qs(`#tab-${btn.dataset.tab}`).classList.add("active");
      });
    });
  }

  // The dropdowns (standards/sections/subjects/teachers) and the assignment
  // tables below them are independent pieces of data. Previously a failure
  // loading the dropdowns (e.g. one failed request) would throw inside a
  // single try/catch that wrapped everything, which meant the assignment
  // tables never even started loading and were left on their spinner
  // forever - with the only sign of a problem being a toast that
  // auto-dismisses after a few seconds. Now each piece loads and fails
  // independently, and dropdown failures show a persistent inline message
  // instead of a toast that can be missed.
  init();

  async function init() {
    loadDropdownData();
    loadClassAssignments();
    loadSubjectAssignments();
  }

  async function loadDropdownData() {
    Utils.qs("#ct-form-error").innerHTML = "";
    Utils.qs("#st-form-error").innerHTML = "";
    try {
      const [standards, allTeachers] = await Promise.all([AcademicApi.getStandards(), api.get("/api/teachers")]);
      teachers = allTeachers;

      const stdOptions = standards.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("")
        || "<option value=''>No standards yet - add one in Academic Setup</option>";
      Utils.qs("#ct-standard").innerHTML = stdOptions;
      Utils.qs("#st-standard").innerHTML = stdOptions;

      Utils.qs("#ct-teacher").innerHTML = teachers
        .filter((t) => (t.role === "CLASS_TEACHER" || t.role === "SUBJECT_TEACHER") && t.active)
        .map((t) => `<option value="${t.id}">${Utils.escapeHtml(t.fullName)}</option>`)
        .join("") || "<option value=''>No active teachers</option>";
      Utils.qs("#st-teacher").innerHTML = teachers
        .filter((t) => (t.role === "SUBJECT_TEACHER" || t.role === "CLASS_TEACHER") && t.active)
        .map((t) => `<option value="${t.id}">${Utils.escapeHtml(t.fullName)}</option>`)
        .join("") || "<option value=''>No active teachers</option>";

      Utils.qs("#ct-standard").addEventListener("change", () => populateSections("#ct-standard", "#ct-section"));
      Utils.qs("#st-standard").addEventListener("change", async () => {
        await populateSections("#st-standard", "#st-section");
        await populateSubjects();
      });
      // Subjects are offered per section/group (e.g. a 12th Bio-Maths group
      // never offers History), not just per standard - so the subject list
      // must also refresh whenever the admin changes the section/group
      // itself, not only when the standard changes.
      Utils.qs("#st-section").addEventListener("change", populateSubjects);

      await populateSections("#ct-standard", "#ct-section");
      await populateSections("#st-standard", "#st-section");
      await populateSubjects();

      Utils.qs("#ct-form").addEventListener("submit", submitClassTeacherAssignment);
      Utils.qs("#st-form").addEventListener("submit", submitSubjectTeacherAssignment);
    } catch (err) {
      const message = (err && err.message) || "Could not load standards/teachers for the dropdowns.";
      const banner = `<div class="state-block"><div class="state-icon">⚠️</div><div>${Utils.escapeHtml(message)}</div>
        <button class="btn btn-secondary mt-10" id="dropdown-retry-btn">Retry</button></div>`;
      Utils.qs("#ct-form-error").innerHTML = banner;
      Utils.qs("#dropdown-retry-btn").onclick = loadDropdownData;
    }
  }

  async function populateSections(standardSel, sectionSel) {
    const standardId = Utils.qs(standardSel).value;
    const sel = Utils.qs(sectionSel);
    if (!standardId) { sel.innerHTML = ""; return; }
    const sections = await AcademicApi.getSections(standardId);
    sel.innerHTML = sections.map((sg) => `<option value="${sg.id}">${Utils.escapeHtml(sg.name)}</option>`).join("") || "<option value=''>No sections</option>";
  }

  async function populateSubjects() {
    // Scoped to the currently-selected section/group (via subject_section_groups),
    // not the whole standard - a subject teacher assignment must only ever
    // offer subjects that are actually taught in that specific section/group.
    const sectionGroupId = Utils.qs("#st-section").value;
    const sel = Utils.qs("#st-subject");
    const hint = Utils.qs("#st-subject-hint");
    const submitBtn = Utils.qs("#st-submit-btn");
    if (!sectionGroupId) { sel.innerHTML = ""; hint.innerHTML = ""; return; }
    const subjects = await AcademicApi.getSubjectsForSectionGroup(sectionGroupId);
    if (subjects.length) {
      sel.disabled = false;
      sel.innerHTML = subjects.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("");
      hint.innerHTML = "";
      if (submitBtn) submitBtn.disabled = false;
    } else {
      // Empty select (not a fake disabled option with an unusable value) so
      // the "select a subject" validation below still catches it cleanly.
      sel.disabled = true;
      sel.innerHTML = "";
      hint.innerHTML = `<span class="text-faint">No subjects are mapped to this section/group yet.</span>
        <a href="academic-setup.html#sub">Map subjects in Academic Setup →</a>`;
      if (submitBtn) submitBtn.disabled = true;
    }
  }

  async function loadClassAssignments() {
    const region = Utils.qs("#ct-table");
    Utils.setLoading(region, "Loading assignments...");
    try {
      const list = await api.get("/api/class-teacher-assignments");
      if (!list.length) { Utils.setEmpty(region, "No class teacher assignments yet."); return; }
      region.innerHTML = renderAssignmentTable(list, false);
      wireRemove(region, "/api/class-teacher-assignments", loadClassAssignments);
    } catch (err) {
      Utils.setError(region, err.message, loadClassAssignments);
    }
  }

  async function loadSubjectAssignments() {
    const region = Utils.qs("#st-table");
    Utils.setLoading(region, "Loading assignments...");
    try {
      const list = await api.get("/api/subject-teacher-assignments");
      if (!list.length) { Utils.setEmpty(region, "No subject teacher assignments yet."); return; }
      region.innerHTML = renderAssignmentTable(list, true);
      wireRemove(region, "/api/subject-teacher-assignments", loadSubjectAssignments);
    } catch (err) {
      Utils.setError(region, err.message, loadSubjectAssignments);
    }
  }

  function renderAssignmentTable(list, withSubject) {
    return `
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr><th>Standard</th><th>Section/Group</th>${withSubject ? "<th>Subject</th>" : ""}<th>Teacher</th><th>Status</th><th></th></tr></thead>
          <tbody>
            ${list
              .map(
                (a) => `
              <tr>
                <td>${Utils.escapeHtml(a.standardName)}</td>
                <td>${Utils.escapeHtml(a.sectionGroupName)}</td>
                ${withSubject ? `<td>${Utils.escapeHtml(a.subjectName)}</td>` : ""}
                <td>${Utils.escapeHtml(a.teacherName)}</td>
                <td>${a.active ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-danger">Inactive</span>'}</td>
                <td class="text-right"><button class="btn btn-sm btn-outline-danger" data-id="${a.id}">Remove</button></td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>`;
  }

  function wireRemove(region, basePath, reload) {
    Utils.qsa("button[data-id]", region).forEach((btn) => {
      btn.addEventListener("click", async () => {
        const ok = await Utils.confirmDialog({ title: "Remove assignment?", message: "This will unassign the teacher from this class.", confirmText: "Remove" });
        if (!ok) return;
        try {
          await api.del(`${basePath}/${btn.dataset.id}`);
          Utils.toast("Assignment removed.", "success");
          reload();
        } catch (err) {
          Utils.errorToast(err);
        }
      });
    });
  }

  async function submitClassTeacherAssignment(e) {
    e.preventDefault();
    const sectionGroupId = Utils.qs("#ct-section").value;
    const teacherId = Utils.qs("#ct-teacher").value;
    if (!sectionGroupId || !teacherId) { Utils.toast("Please select a section and a teacher.", "error"); return; }
    try {
      await api.post("/api/class-teacher-assignments", { sectionGroupId: Number(sectionGroupId), teacherId: Number(teacherId) });
      Utils.toast("Class teacher assigned successfully.", "success");
      loadClassAssignments();
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  async function submitSubjectTeacherAssignment(e) {
    e.preventDefault();
    const sectionGroupId = Utils.qs("#st-section").value;
    const subjectId = Utils.qs("#st-subject").value;
    const teacherId = Utils.qs("#st-teacher").value;
    if (!sectionGroupId || !subjectId || !teacherId) { Utils.toast("Please select a section, subject and teacher.", "error"); return; }
    try {
      await api.post("/api/subject-teacher-assignments", {
        sectionGroupId: Number(sectionGroupId),
        subjectId: Number(subjectId),
        teacherId: Number(teacherId),
      });
      Utils.toast("Subject teacher assigned successfully.", "success");
      loadSubjectAssignments();
    } catch (err) {
      Utils.errorToast(err);
    }
  }
})();
