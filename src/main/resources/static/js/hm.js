(function () {
  const user = Shell.init({ allowedRoles: ["HEADMASTER"], activeKey: "dashboard", title: "HM Dashboard" });
  if (!user) return;

  loadDashboard();

  async function loadDashboard() {
    const statsRegion = Utils.qs("#stats-region");
    try {
      const data = await api.get("/api/dashboard/hm");
      const perStandard = data.studentsPerStandard || {};
      const countFor = (num) => {
        const key =
          Object.keys(perStandard).find((k) => k.trim() === num) ||
          Object.keys(perStandard).find((k) => k.replace(/\D/g, "") === num);
        return key ? perStandard[key] : 0;
      };
      const tenth = countFor("10");
      const twelfth = countFor("12");

      statsRegion.innerHTML = `
        <div class="stat-grid">
          <div class="stat-card accent-primary">
            <div class="stat-label">Total Students</div>
            <div class="stat-value">${data.totalStudents}</div>
            <div class="stat-icon">\u{1F464}</div>
          </div>
          <div class="stat-card accent-primary">
            <div class="stat-label">10th Standard</div>
            <div class="stat-value">${tenth}</div>
            <div class="stat-icon">\u{1F4D8}</div>
          </div>
          <div class="stat-card accent-primary">
            <div class="stat-label">12th Standard</div>
            <div class="stat-value">${twelfth}</div>
            <div class="stat-icon">\u{1F393}</div>
          </div>
          <div class="stat-card accent-accent">
            <div class="stat-label">Total Teachers</div>
            <div class="stat-value">${data.totalTeachers}</div>
            <div class="stat-icon">\u{1F9D1}\u200D\u{1F3EB}</div>
          </div>
          <div class="stat-card accent-accent">
            <div class="stat-label">Class Teachers</div>
            <div class="stat-value">${data.totalClassTeachers}</div>
            <div class="stat-icon">\u{1F3EB}</div>
          </div>
          <div class="stat-card accent-accent">
            <div class="stat-label">Subject Teachers</div>
            <div class="stat-value">${data.totalSubjectTeachers}</div>
            <div class="stat-icon">\u{1F4D6}</div>
          </div>
        </div>`;

      const breakdown = Utils.qs("#section-breakdown");
      const entries = Object.entries(data.studentsPerSectionGroup || {});
      if (!entries.length) {
        Utils.setEmpty(breakdown, "No sections have students yet.");
      } else {
        breakdown.innerHTML = entries
          .map(
            ([name, count]) => `
            <div class="flex-between mb-10">
              <span>${Utils.escapeHtml(name)}</span>
              <span class="badge badge-primary">${count} students</span>
            </div>`
          )
          .join("");
      }
    } catch (err) {
      Utils.setError(statsRegion, err.message, loadDashboard);
    }
  }
})();
