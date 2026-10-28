(function () {
  const user = Shell.init({ allowedRoles: ["CLASS_TEACHER"], activeKey: "dashboard", title: "Class Teacher Dashboard" });
  if (!user) return;

  load();

  async function load() {
    const region = Utils.qs("#dash-region");
    try {
      const dash = await api.get("/api/dashboard/class-teacher");
      region.innerHTML = `
        <div class="card mb-16">
          <div class="flex-between flex-wrap gap-14">
            <div>
              <div class="text-faint" style="font-size:12.5px; text-transform:uppercase; font-weight:700;">My Assigned Class</div>
              <h1 class="mb-0">${Utils.escapeHtml(dash.standardName)} - ${Utils.escapeHtml(dash.sectionGroupName)}</h1>
              <p class="mb-0">Class Teacher: ${Utils.escapeHtml(user.username)}</p>
            </div>
            <div class="stat-grid" style="margin-bottom:0;">
              <div class="stat-card accent-primary">
                <div class="stat-label">Students</div>
                <div class="stat-value">${dash.studentCount}</div>
              </div>
              <div class="stat-card ${dash.attendanceTakenToday ? "accent-accent" : "accent-warning"}">
                <div class="stat-label">Today's Attendance</div>
                <div class="stat-value" style="font-size:18px;">
                  ${dash.attendanceTakenToday ? `Submitted (${dash.classAttendancePercentageToday}%)` : "Not Submitted"}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="tile-grid">
          <div class="tile-card" onclick="location.href='students.html?scope=myclass'">
            <div class="tile-icon">\u{1F465}</div>
            <div class="tile-label">My Students</div>
          </div>
          <div class="tile-card" onclick="location.href='attendance.html'">
            <div class="tile-icon">\u2705</div>
            <div class="tile-label">Daily Attendance</div>
          </div>
          <div class="tile-card" onclick="location.href='attendance-history.html'">
            <div class="tile-icon">\u{1F4C6}</div>
            <div class="tile-label">Attendance History</div>
          </div>
        </div>`;
    } catch (err) {
      Utils.setError(region, err.message, load);
    }
  }
})();
