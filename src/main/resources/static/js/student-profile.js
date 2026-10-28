(function () {
  const params = new URLSearchParams(location.search);
  const studentId = params.get("id");

  const preUser = Auth.getUser();
  const activeKey = preUser && preUser.role === "CLASS_TEACHER" ? "myclass" : "students";
  const user = Shell.init({
    allowedRoles: ["HEADMASTER", "ADMIN", "CLASS_TEACHER", "SUBJECT_TEACHER"],
    activeKey,
    title: "Student Profile",
  });
  if (!user) return;

  const canManage = user.role === "ADMIN" || user.role === "CLASS_TEACHER";
  Utils.qs("#crumb-back").addEventListener("click", (e) => {
    e.preventDefault();
    history.length > 1 ? history.back() : (location.href = "students.html");
  });

  const DOC_TYPES = [
    ["AADHAAR_CARD", "Aadhaar Card"],
    ["BIRTH_CERTIFICATE", "Birth Certificate"],
    ["TRANSFER_CERTIFICATE", "Transfer Certificate"],
    ["COMMUNITY_CERTIFICATE", "Community Certificate"],
    ["STUDENT_PHOTO", "Student Photo"],
    ["OTHER", "Other"],
  ];
  const docTypeLabel = (t) => (DOC_TYPES.find((d) => d[0] === t) || [t, t])[1];

  let student = null;

  loadProfile();
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

  async function loadProfile() {
    const region = Utils.qs("#profile-region");
    try {
      student = await api.get(`/api/students/${studentId}`);
      renderHeader();
      Utils.qs("#profile-body").classList.remove("hidden");
      loadDocuments();
      loadAcademic();
      loadAttendance();
      if (canManage) Utils.qs("#upload-doc-btn").classList.remove("hidden");
      // Admin edits everything; the class teacher fills in / updates the detailed fields.
      region.innerHTML = canManage
        ? `<div style="margin-bottom:12px;"><a class="btn btn-secondary" href="student-form.html?id=${student.id}">\u270F\uFE0F ${user.role === "ADMIN" ? "Edit student" : "Edit details"}</a></div>`
        : "";
    } catch (err) {
      Utils.setError(region, err.message, loadProfile);
    }
  }

  function renderHeader() {
    const region = Utils.qs("#profile-region");
    region.innerHTML = `
      <div class="card">
        <div class="profile-header">
          <div class="profile-photo" id="profile-photo-el">${Utils.initials(student.name)}</div>
          <div>
            <h1 class="mb-0">${Utils.escapeHtml(student.name)}</h1>
            <p class="mb-0">${Utils.escapeHtml(student.standardName)} - ${Utils.escapeHtml(student.sectionGroupName)}
              &nbsp;\u2022&nbsp; Register No: ${Utils.escapeHtml(student.registerNumber || "-")}
              &nbsp;\u2022&nbsp; ${student.active ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-danger">Inactive</span>'}
            </p>
          </div>
        </div>
      </div>`;
    region.insertAdjacentHTML("afterend", "");

    Utils.qs("#personal-info").innerHTML = [
      ["Admission No", student.admissionNumber],
      ["Register No", student.registerNumber],
      ["EMIS Number", student.emisNumber],
      ["Date of Birth", Utils.formatDate(student.dob)],
      ["Gender", student.gender],
      ["Aadhaar Number", student.aadhaarNumber],
      ["Address", student.address],
    ]
      .map(([label, value]) => `<div><div class="meta-label">${label}</div><div class="meta-value">${Utils.escapeHtml(value || "-")}</div></div>`)
      .join("");

    Utils.qs("#parent-info").innerHTML = [
      ["Father's Name", student.fatherName],
      ["Mother's Name", student.motherName],
      ["Guardian", student.guardianName],
      ["Parent Phone", student.parentPhone],
      ["Parent Email", student.parentEmail],
    ]
      .map(([label, value]) => `<div><div class="meta-label">${label}</div><div class="meta-value">${Utils.escapeHtml(value || "-")}</div></div>`)
      .join("");
  }

  // ---------------- Documents ----------------
  async function loadDocuments() {
    const region = Utils.qs("#documents-region");
    Utils.setLoading(region, "Loading documents...");
    try {
      const docs = await api.get(`/api/students/${studentId}/documents`);
      const photoDoc = docs.find((d) => d.documentType === "STUDENT_PHOTO");
      if (photoDoc) renderPhoto(photoDoc);

      if (!docs.length) {
        Utils.setEmpty(region, "No documents uploaded for this student yet.");
      } else {
        region.innerHTML = docs
          .map(
            (d) => `
          <div class="doc-row">
            <div class="doc-info">
              <div class="doc-icon">\u{1F4C4}</div>
              <div>
                <div style="font-weight:600;">${docTypeLabel(d.documentType)}</div>
                <div class="text-faint" style="font-size:12px;">${Utils.escapeHtml(d.originalFilename)} \u2022 ${(d.fileSize / 1024).toFixed(0)} KB \u2022 Uploaded ${Utils.formatDate(d.uploadedDate)}</div>
              </div>
            </div>
            <div class="flex gap-8">
              <button class="btn btn-sm btn-secondary" data-action="view" data-id="${d.id}">View</button>
              <button class="btn btn-sm btn-secondary" data-action="download" data-id="${d.id}" data-name="${Utils.escapeHtml(d.originalFilename)}">Download</button>
              ${canManage ? `<button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${d.id}">Delete</button>` : ""}
            </div>
          </div>`
          )
          .join("");
      }
      Utils.qsa("[data-action]", region).forEach((btn) => {
        btn.addEventListener("click", () => handleDocAction(btn.dataset.action, btn.dataset.id, btn.dataset.name));
      });
    } catch (err) {
      Utils.setError(region, err.message, loadDocuments);
    }
  }

  async function renderPhoto(photoDoc) {
    try {
      const { blob } = await api.download(`/api/documents/${photoDoc.id}`);
      const url = URL.createObjectURL(blob);
      Utils.qs("#profile-photo-el").innerHTML = `<img src="${url}" alt="Student photo" />`;
    } catch {
      /* keep initials fallback */
    }
  }

  async function handleDocAction(action, id, filename) {
    if (action === "view" || action === "download") {
      try {
        const { blob, filename: serverName } = await api.download(`/api/documents/${id}`);
        const url = URL.createObjectURL(blob);
        if (action === "view") {
          window.open(url, "_blank");
        } else {
          const a = document.createElement("a");
          a.href = url;
          a.download = filename || serverName;
          a.click();
        }
      } catch (err) {
        Utils.errorToast(err);
      }
    } else if (action === "delete") {
      const ok = await Utils.confirmDialog({ title: "Delete document?", message: "This document will be permanently removed.", confirmText: "Delete" });
      if (!ok) return;
      try {
        await api.del(`/api/documents/${id}`);
        Utils.toast("Document deleted.", "success");
        loadDocuments();
      } catch (err) {
        Utils.errorToast(err);
      }
    }
  }

  Utils.qs("#upload-doc-btn").addEventListener("click", openUploadModal);

  function openUploadModal() {
    Utils.openModal(`
      <h3>Upload Document</h3>
      <p>Choose the document type and file to upload for ${Utils.escapeHtml(student.name)}.</p>
      <form id="upload-form">
        <div class="form-group">
          <label>Document Type <span class="req">*</span></label>
          <select name="documentType" required>
            ${DOC_TYPES.map(([v, l]) => `<option value="${v}">${l}</option>`).join("")}
          </select>
        </div>
        <div class="form-group">
          <label>File <span class="req">*</span></label>
          <input type="file" name="file" accept=".pdf,.jpg,.jpeg,.png" required />
          <div class="text-faint" style="font-size:12px; margin-top:4px;">PDF, JPG or PNG. Max 5 MB.</div>
        </div>
        <div id="upload-progress" class="hidden"><div class="spinner"></div></div>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" id="upload-cancel">Cancel</button>
          <button type="submit" class="btn btn-primary" id="upload-submit">Upload</button>
        </div>
      </form>
    `);
    Utils.qs("#upload-cancel").addEventListener("click", Utils.closeModal);
    Utils.qs("#upload-form").addEventListener("submit", async (e) => {
      e.preventDefault();
      const form = e.target;
      const file = form.file.files[0];
      if (!file) return;
      if (file.size > 5 * 1024 * 1024) {
        Utils.toast("File exceeds the 5 MB limit.", "error");
        return;
      }
      const btn = Utils.qs("#upload-submit");
      Utils.setButtonLoading(btn, true, "Uploading...");
      try {
        const fd = new FormData();
        fd.append("file", file);
        await api.upload(`/api/students/${studentId}/documents?documentType=${form.documentType.value}`, fd);
        Utils.toast("Document uploaded successfully.", "success");
        Utils.closeModal();
        loadDocuments();
      } catch (err) {
        Utils.errorToast(err);
        Utils.setButtonLoading(btn, false);
      }
    });
  }

  // ---------------- Academic performance ----------------
  async function loadAcademic() {
    const select = Utils.qs("#academic-exam-select");
    const region = Utils.qs("#academic-region");
    try {
      const [exams, marks] = await Promise.all([
        AcademicApi.getExams(student.standardId),
        api.get(`/api/marks/student/${studentId}`),
      ]);
      if (!exams.length) {
        select.innerHTML = "<option>No exams available</option>";
        Utils.setEmpty(region, "No exams have been set up yet.");
        return;
      }
      select.innerHTML = exams.map((e) => `<option value="${e.id}">${Utils.escapeHtml(e.name)}</option>`).join("");
      select.value = exams[exams.length - 1].id;
      select.addEventListener("change", () => renderMarksForExam(marks, select.value));
      renderMarksForExam(marks, select.value);
    } catch (err) {
      Utils.setError(region, err.message, loadAcademic);
    }
  }

  function renderMarksForExam(allMarks, examId) {
    const region = Utils.qs("#academic-region");
    const rows = allMarks.filter((m) => String(m.examId) === String(examId));
    if (!rows.length) {
      Utils.setEmpty(region, "No marks recorded for this exam yet.");
      return;
    }
    const totalObtained = rows.reduce((s, r) => s + r.marksObtained, 0);
    const totalMax = rows.reduce((s, r) => s + r.maxMarks, 0);
    region.innerHTML = `
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr><th>Subject</th><th>Marks Obtained</th><th>Max Marks</th></tr></thead>
          <tbody>
            ${rows.map((r) => `<tr><td>${Utils.escapeHtml(r.subjectName)}</td><td>${r.marksObtained}</td><td>${r.maxMarks}</td></tr>`).join("")}
            <tr style="font-weight:700;"><td>Total</td><td>${totalObtained}</td><td>${totalMax}</td></tr>
          </tbody>
        </table>
      </div>
      <p class="mt-10">Overall: <strong>${totalMax ? Math.round((totalObtained / totalMax) * 10000) / 100 : 0}%</strong></p>`;
  }

  // ---------------- Attendance ----------------
  async function loadAttendance() {
    const statsRegion = Utils.qs("#attendance-stats");
    const historyRegion = Utils.qs("#attendance-history-region");
    try {
      const [pct, history] = await Promise.all([
        api.get(`/api/class-attendance/student/${studentId}/percentage`),
        api.get(`/api/class-attendance/student/${studentId}`),
      ]);
      statsRegion.innerHTML = `
        <div class="stat-card accent-primary">
          <div class="stat-label">Attendance Percentage</div>
          <div class="stat-value">${pct.percentage}%</div>
        </div>
        <div class="stat-card accent-accent">
          <div class="stat-label">Days Present</div>
          <div class="stat-value">${pct.presentCount}</div>
        </div>
        <div class="stat-card accent-warning">
          <div class="stat-label">Total Days Recorded</div>
          <div class="stat-value">${pct.totalCount}</div>
        </div>`;

      if (!history.length) {
        Utils.setEmpty(historyRegion, "No attendance has been recorded yet.");
        return;
      }
      const sorted = [...history].sort((a, b) => new Date(b.date) - new Date(a.date));
      historyRegion.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Date</th><th>Status</th></tr></thead>
            <tbody>
              ${sorted
                .map(
                  (h) => `<tr><td>${Utils.formatDate(h.date)}</td><td>${
                    h.status === "PRESENT" ? '<span class="badge badge-success">Present</span>' : '<span class="badge badge-danger">Absent</span>'
                  }</td></tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>`;
    } catch (err) {
      Utils.setError(statsRegion, err.message, loadAttendance);
    }
  }
})();
