(function () {
  const user = Shell.init({ allowedRoles: ["SUBJECT_TEACHER", "CLASS_TEACHER"], activeKey: (Auth.getUser() && Auth.getUser().role === "CLASS_TEACHER") ? "subjects" : "dashboard", title: "Subject Teacher Dashboard" });
  if (!user) return;

  load();

  async function load() {
    const region = Utils.qs("#dash-region");
    try {
      const dash = await api.get("/api/dashboard/subject-teacher");

      const statsHtml = `
        <div class="stat-grid">
          <div class="stat-card accent-primary">
            <div class="stat-label">Subjects Taught</div>
            <div class="stat-value">${(dash.assignedSubjects || []).length}</div>
            <div class="stat-icon">\u{1F4D6}</div>
          </div>
          <div class="stat-card accent-primary">
            <div class="stat-label">Classes Assigned</div>
            <div class="stat-value">${(dash.assignedClasses || []).length}</div>
            <div class="stat-icon">\u{1F3EB}</div>
          </div>
          <div class="stat-card accent-accent">
            <div class="stat-label">Total Students Taught</div>
            <div class="stat-value">${dash.totalStudentsTaught}</div>
            <div class="stat-icon">\u{1F465}</div>
          </div>
        </div>`;

      const assignments = dash.assignments || [];
      let assignmentsHtml;
      if (!assignments.length) {
        assignmentsHtml = `<div class="card"><div class="state-block"><div class="state-icon">\u{1F4ED}</div>You have no class/subject assignments yet. Contact your administrator.</div></div>`;
      } else {
        assignmentsHtml = `
          <div class="card">
            <h2>My Assignments</h2>
            <p>Click a class to enter marks.</p>
            <div class="tile-grid">
              ${assignments
                .map(
                  (a) => `
                <div class="tile-card" data-sgid="${a.sectionGroupId}" data-sgname="${Utils.escapeHtml(a.standardName)} - ${Utils.escapeHtml(a.sectionGroupName)}" data-subid="${a.subjectId}" data-subname="${Utils.escapeHtml(a.subjectName)}" data-action="marks">
                  <div class="tile-icon">\u{1F4D8}</div>
                  <div class="tile-label">${Utils.escapeHtml(a.standardName)} - ${Utils.escapeHtml(a.sectionGroupName)}</div>
                  <div class="tile-sub">${Utils.escapeHtml(a.subjectName)}</div>
                </div>`
                )
                .join("")}
            </div>
          </div>`;
      }

      region.innerHTML = statsHtml + assignmentsHtml;

      Utils.qsa('[data-action="marks"]', region).forEach((tile) => {
        tile.addEventListener("click", () => {
          const q = `?sectionGroupId=${tile.dataset.sgid}&sectionGroupName=${encodeURIComponent(tile.dataset.sgname)}&subjectId=${tile.dataset.subid}&subjectName=${encodeURIComponent(tile.dataset.subname)}`;
          location.href = "marks.html" + q;
        });
      });
    } catch (err) {
      Utils.setError(region, err.message, load);
    }
  }
})();
