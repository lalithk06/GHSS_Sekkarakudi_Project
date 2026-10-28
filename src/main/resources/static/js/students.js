(function () {
  const params = new URLSearchParams(location.search);
  const presetSectionGroupId = params.get("sectionGroupId") || "";
  const scope = params.get("scope") || "all";

  const preUser = Auth.getUser();
  const activeKey = preUser && preUser.role === "CLASS_TEACHER" ? "myclass" : "students";

  const user = Shell.init({
    allowedRoles: ["HEADMASTER", "ADMIN", "CLASS_TEACHER", "SUBJECT_TEACHER"],
    activeKey,
    title: "Students",
  });
  if (!user) return;

  // Only the class teacher owns their section's student records - Admin/HM are read-only here.
  const canEdit = user.role === "ADMIN";
  if (canEdit) Utils.qs("#add-student-btn").classList.remove("hidden");
  if (user.role === "CLASS_TEACHER") {
    Utils.qs("#page-sub").textContent = "Students in your assigned class.";
  }

  // A class teacher only ever sees their own class: no standard/section filters.
  if (user.role === "CLASS_TEACHER") {
    ["#standard-filter", "#section-filter"].forEach((sel) => { const el = Utils.qs(sel); if (el) el.classList.add("hidden"); });
  }

  let allStudents = [];
  let currentPage = 1;
  const PAGE_SIZE = 10;

  const searchInput = Utils.qs("#search-input");
  const standardFilter = Utils.qs("#standard-filter");
  const sectionFilter = Utils.qs("#section-filter");

  initFilters();
  fetchStudents();

  searchInput.addEventListener("input", Utils.debounce(() => { currentPage = 1; fetchStudents(); }, 400));
  standardFilter.addEventListener("change", async () => {
    await populateSections(standardFilter.value);
    currentPage = 1;
    fetchStudents();
  });
  sectionFilter.addEventListener("change", () => { currentPage = 1; fetchStudents(); });

  async function initFilters() {
    try {
      const standards = await AcademicApi.getStandards();
      standardFilter.insertAdjacentHTML(
        "beforeend",
        standards.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("")
      );
      if (presetSectionGroupId) {
        const sg = await api.get(`/api/sections/${presetSectionGroupId}`);
        standardFilter.value = sg.standardId;
        await populateSections(sg.standardId);
        sectionFilter.value = presetSectionGroupId;
      }
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  async function populateSections(standardId) {
    sectionFilter.innerHTML = '<option value="">All Sections/Groups</option>';
    if (!standardId) return;
    const sections = await AcademicApi.getSections(standardId);
    sectionFilter.insertAdjacentHTML(
      "beforeend",
      sections.map((sg) => `<option value="${sg.id}">${Utils.escapeHtml(sg.name)}</option>`).join("")
    );
  }

  async function fetchStudents() {
    const region = Utils.qs("#students-region");
    Utils.setLoading(region, "Loading students...");
    try {
      allStudents = await api.get("/api/students", {
        search: searchInput.value.trim() || undefined,
        standardId: standardFilter.value || undefined,
        sectionGroupId: sectionFilter.value || undefined,
      });
      renderPage();
    } catch (err) {
      Utils.setError(region, err.message, fetchStudents);
    }
  }

  function renderPage() {
    const region = Utils.qs("#students-region");
    if (!allStudents.length) {
      Utils.setEmpty(region, "No students found matching your search.");
      Utils.qs("#pagination").innerHTML = "";
      return;
    }

    const totalPages = Math.ceil(allStudents.length / PAGE_SIZE);
    currentPage = Math.min(currentPage, totalPages) || 1;
    const pageItems = allStudents.slice((currentPage - 1) * PAGE_SIZE, currentPage * PAGE_SIZE);

    region.innerHTML = `
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr>
            <th>Name</th><th>Register No</th><th>Admission No</th><th>EMIS</th>
            <th>Standard</th><th>Section/Group</th><th>Status</th><th></th>
          </tr></thead>
          <tbody>
            ${pageItems
              .map(
                (s) => `
              <tr>
                <td class="clickable" data-id="${s.id}" data-action="view">${Utils.escapeHtml(s.name)}</td>
                <td>${Utils.escapeHtml(s.registerNumber || "-")}</td>
                <td>${Utils.escapeHtml(s.admissionNumber)}</td>
                <td>${Utils.escapeHtml(s.emisNumber || "-")}</td>
                <td>${Utils.escapeHtml(s.standardName)}</td>
                <td>${Utils.escapeHtml(s.sectionGroupName)}</td>
                <td>${s.active ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-danger">Inactive</span>'}</td>
                <td class="text-right">
                  <button class="btn btn-sm btn-secondary" data-id="${s.id}" data-action="view">View</button>
                  ${
                    canEdit
                      ? `<button class="btn btn-sm btn-secondary" data-id="${s.id}" data-action="edit">Edit</button>
                         <button class="btn btn-sm btn-outline-danger" data-id="${s.id}" data-action="delete">Delete</button>`
                      : user.role === "CLASS_TEACHER"
                        ? `<button class="btn btn-sm btn-secondary" data-id="${s.id}" data-action="edit">Details</button>`
                        : ""
                  }
                </td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>`;

    Utils.qsa("[data-action]", region).forEach((el) => {
      el.addEventListener("click", () => handleAction(el.dataset.action, el.dataset.id));
    });

    renderPagination(totalPages);
  }

  function renderPagination(totalPages) {
    const pager = Utils.qs("#pagination");
    if (totalPages <= 1) { pager.innerHTML = ""; return; }
    let html = `<button ${currentPage === 1 ? "disabled" : ""} data-page="${currentPage - 1}">\u2039</button>`;
    for (let i = 1; i <= totalPages; i++) {
      html += `<button class="${i === currentPage ? "active" : ""}" data-page="${i}">${i}</button>`;
    }
    html += `<button ${currentPage === totalPages ? "disabled" : ""} data-page="${currentPage + 1}">\u203A</button>`;
    pager.innerHTML = html;
    Utils.qsa("button[data-page]", pager).forEach((btn) => {
      btn.addEventListener("click", () => { currentPage = Number(btn.dataset.page); renderPage(); });
    });
  }

  async function handleAction(action, id) {
    if (action === "view") { location.href = `student-profile.html?id=${id}`; return; }
    if (action === "edit") { location.href = `student-form.html?id=${id}`; return; }
    if (action === "delete") {
      const ok = await Utils.confirmDialog({
        title: "Delete student?",
        message: "This will permanently remove the student record. This cannot be undone.",
        confirmText: "Delete",
      });
      if (!ok) return;
      try {
        await api.del(`/api/students/${id}`);
        Utils.toast("Student deleted successfully.", "success");
        fetchStudents();
      } catch (err) {
        Utils.errorToast(err);
      }
    }
  }
})();
