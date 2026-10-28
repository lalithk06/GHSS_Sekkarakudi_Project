(function () {
  const user = Shell.init({ allowedRoles: ["CLASS_TEACHER"], activeKey: "history", title: "Attendance History" });
  if (!user) return;

  const select = Utils.qs("#student-select");
  const region = Utils.qs("#history-region");

  init();
  select.addEventListener("change", () => loadHistory(select.value));

  async function init() {
    try {
      const dash = await api.get("/api/dashboard/class-teacher");
      const students = await api.get("/api/students", { sectionGroupId: dash.sectionGroupId });
      if (!students.length) {
        select.innerHTML = "<option>No students</option>";
        Utils.setEmpty(region, "No students found in your class yet.");
        return;
      }
      select.innerHTML = students.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)} (${s.registerNumber || s.admissionNumber})</option>`).join("");
      loadHistory(select.value);
    } catch (err) {
      Utils.setError(region, err.message, init);
    }
  }

  async function loadHistory(studentId) {
    Utils.setLoading(region, "Loading attendance history...");
    try {
      const [pct, history] = await Promise.all([
        api.get(`/api/class-attendance/student/${studentId}/percentage`),
        api.get(`/api/class-attendance/student/${studentId}`),
      ]);
      if (!history.length) {
        Utils.setEmpty(region, "No attendance recorded yet for this student.");
        return;
      }
      const sorted = [...history].sort((a, b) => new Date(b.date) - new Date(a.date));
      region.innerHTML = `
        <div class="stat-grid">
          <div class="stat-card accent-primary"><div class="stat-label">Attendance Percentage</div><div class="stat-value">${pct.percentage}%</div></div>
          <div class="stat-card accent-accent"><div class="stat-label">Days Present</div><div class="stat-value">${pct.presentCount}</div></div>
          <div class="stat-card accent-warning"><div class="stat-label">Total Days Recorded</div><div class="stat-value">${pct.totalCount}</div></div>
        </div>
        <div class="card">
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
          </div>
        </div>`;
    } catch (err) {
      Utils.setError(region, err.message, () => loadHistory(studentId));
    }
  }
})();
