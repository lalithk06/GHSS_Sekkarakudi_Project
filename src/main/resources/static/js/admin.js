(function () {
  const user = Shell.init({ allowedRoles: ["ADMIN"], activeKey: "dashboard", title: "Admin Dashboard" });
  if (!user) return;

  load();

  async function load() {
    const region = Utils.qs("#stats-region");
    try {
      const data = await api.get("/api/dashboard/admin");
      region.innerHTML = `
        <div class="stat-grid">
          <div class="stat-card accent-primary"><div class="stat-label">Total Teachers</div><div class="stat-value">${data.totalTeachers}</div></div>
          <div class="stat-card accent-primary"><div class="stat-label">Class Teachers</div><div class="stat-value">${data.classTeachers}</div></div>
          <div class="stat-card accent-primary"><div class="stat-label">Subject Teachers</div><div class="stat-value">${data.subjectTeachers}</div></div>
          <div class="stat-card accent-accent"><div class="stat-label">Total Subjects</div><div class="stat-value">${data.totalSubjects}</div></div>
          <div class="stat-card accent-accent"><div class="stat-label">Standards</div><div class="stat-value">${data.totalStandards}</div></div>
          <div class="stat-card accent-accent"><div class="stat-label">Sections/Groups</div><div class="stat-value">${data.totalSectionGroups}</div></div>
          <div class="stat-card accent-warning"><div class="stat-label">Class Teacher Assignments</div><div class="stat-value">${data.classTeacherAssignments}</div></div>
          <div class="stat-card accent-warning"><div class="stat-label">Subject Teacher Assignments</div><div class="stat-value">${data.subjectTeacherAssignments}</div></div>
        </div>`;
      renderAttendanceToday(data.todaysAttendance || []);
    } catch (err) {
      Utils.setError(region, err.message, load);
    }
  }

  function renderAttendanceToday(rows) {
    const region = Utils.qs("#attendance-today");
    if (!region) return;
    if (!rows.length) {
      Utils.setEmpty(region, "No sections have been set up yet.");
      return;
    }
    region.innerHTML = `
      <div class="table-scroll">
        <table class="data-table">
          <thead><tr><th>Standard</th><th>Section/Group</th><th>Students</th><th>Status</th><th>Present %</th></tr></thead>
          <tbody>
            ${rows
              .map(
                (r) => `
              <tr>
                <td>${Utils.escapeHtml(r.standardName)}</td>
                <td class="clickable" data-id="${r.sectionGroupId}" data-std="${Utils.escapeHtml(r.standardName)}">${Utils.escapeHtml(r.sectionGroupName)}</td>
                <td>${r.studentCount}</td>
                <td>${
                  r.attendanceTakenToday
                    ? '<span class="badge badge-success">Submitted</span>'
                    : '<span class="badge badge-danger">Not submitted</span>'
                }</td>
                <td>${r.attendanceTakenToday ? r.percentageToday + "%" : "-"}</td>
              </tr>`
              )
              .join("")}
          </tbody>
        </table>
      </div>`;
    Utils.qsa("td.clickable", region).forEach((td) => {
      td.addEventListener("click", () => {
        location.href = `students.html?scope=all&sectionGroupId=${td.dataset.id}`;
      });
    });
  }
})();
