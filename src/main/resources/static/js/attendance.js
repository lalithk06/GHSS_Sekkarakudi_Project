(function () {
  const user = Shell.init({ allowedRoles: ["CLASS_TEACHER"], activeKey: "attendance", title: "Daily Attendance" });
  if (!user) return;

  const dateInput = Utils.qs("#date-input");
  dateInput.value = Utils.todayIso();
  dateInput.max = Utils.todayIso();

  let sectionGroupId = null;
  let students = [];
  const statusMap = {}; // studentId -> 'PRESENT' | 'ABSENT'

  init();
  dateInput.addEventListener("change", () => loadForDate());

  async function init() {
    const region = Utils.qs("#attendance-region");
    try {
      const dash = await api.get("/api/dashboard/class-teacher");
      sectionGroupId = dash.sectionGroupId;
      Utils.qs("#class-sub").textContent = `${dash.standardName} - ${dash.sectionGroupName} \u2022 ${dash.studentCount} students`;
      students = await api.get("/api/students", { sectionGroupId });
      loadForDate();
    } catch (err) {
      Utils.setError(region, err.message, init);
    }
  }

  async function loadForDate() {
    const region = Utils.qs("#attendance-region");
    Utils.setLoading(region, "Checking attendance for this date...");
    try {
      const existing = await api.get("/api/class-attendance", { sectionGroupId, date: dateInput.value });
      if (existing.length) {
        renderExisting(existing);
      } else {
        renderEditableForm();
      }
    } catch (err) {
      Utils.setError(region, err.message, loadForDate);
    }
  }

  function renderExisting(records) {
    const region = Utils.qs("#attendance-region");
    const presentCount = records.filter((r) => r.status === "PRESENT").length;
    region.innerHTML = `
      <div class="alert alert-info">Attendance for ${Utils.formatDate(dateInput.value)} has already been submitted (${presentCount}/${records.length} present). It cannot be edited here to prevent accidental changes.</div>
      <div class="card">
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Student</th><th>Status</th><th>Recorded By</th></tr></thead>
            <tbody>
              ${records
                .map(
                  (r) => `<tr><td>${Utils.escapeHtml(r.studentName)}</td><td>${
                    r.status === "PRESENT" ? '<span class="badge badge-success">Present</span>' : '<span class="badge badge-danger">Absent</span>'
                  }</td><td>${Utils.escapeHtml(r.recordedBy)}</td></tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>
      </div>`;
  }

  function renderEditableForm() {
    const region = Utils.qs("#attendance-region");
    if (!students.length) {
      Utils.setEmpty(region, "No students found in your class yet.");
      return;
    }
    students.forEach((s) => { statusMap[s.id] = statusMap[s.id] || "PRESENT"; });

    region.innerHTML = `
      <div class="card">
        <div class="table-toolbar">
          <div class="text-muted">Marking attendance for <strong>${Utils.formatDate(dateInput.value)}</strong></div>
          <button class="btn btn-secondary btn-sm" id="mark-all-btn">\u2705 Mark All Present</button>
        </div>
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Student</th><th>Register No</th><th style="width:220px;">Status</th></tr></thead>
            <tbody id="attendance-tbody">
              ${students
                .map(
                  (s) => `
                <tr>
                  <td>${Utils.escapeHtml(s.name)}</td>
                  <td>${Utils.escapeHtml(s.registerNumber || "-")}</td>
                  <td>
                    <div class="attendance-toggle" data-student-id="${s.id}">
                      <button type="button" class="status-present ${statusMap[s.id] === "PRESENT" ? "active" : ""}" data-status="PRESENT">Present</button>
                      <button type="button" class="status-absent ${statusMap[s.id] === "ABSENT" ? "active" : ""}" data-status="ABSENT">Absent</button>
                    </div>
                  </td>
                </tr>`
                )
                .join("")}
            </tbody>
          </table>
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" id="submit-attendance-btn">Submit Attendance</button>
        </div>
      </div>`;

    Utils.qsa(".attendance-toggle", region).forEach((toggle) => {
      Utils.qsa("button", toggle).forEach((btn) => {
        btn.addEventListener("click", () => {
          const studentId = toggle.dataset.studentId;
          statusMap[studentId] = btn.dataset.status;
          Utils.qsa("button", toggle).forEach((b) => b.classList.remove("active"));
          btn.classList.add("active");
        });
      });
    });

    Utils.qs("#mark-all-btn").addEventListener("click", () => {
      students.forEach((s) => (statusMap[s.id] = "PRESENT"));
      renderEditableForm();
    });

    Utils.qs("#submit-attendance-btn").addEventListener("click", submitAttendance);
  }

  async function submitAttendance() {
    const presentCount = Object.values(statusMap).filter((v) => v === "PRESENT").length;
    const ok = await Utils.confirmDialog({
      title: "Submit attendance?",
      message: `${presentCount} of ${students.length} students marked present for ${Utils.formatDate(dateInput.value)}. This cannot be edited afterwards.`,
      confirmText: "Submit",
      danger: false,
    });
    if (!ok) return;

    const btn = Utils.qs("#submit-attendance-btn");
    Utils.setButtonLoading(btn, true, "Submitting...");
    try {
      await api.post("/api/class-attendance", {
        sectionGroupId,
        date: dateInput.value,
        entries: students.map((s) => ({ studentId: s.id, status: statusMap[s.id] })),
      });
      Utils.toast("Attendance submitted successfully.", "success");
      loadForDate();
    } catch (err) {
      Utils.errorToast(err);
      Utils.setButtonLoading(btn, false);
    }
  }
})();
