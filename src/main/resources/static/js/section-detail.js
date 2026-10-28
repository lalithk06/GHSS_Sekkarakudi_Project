(function () {
  const params = new URLSearchParams(location.search);
  const sectionGroupId = params.get("sectionGroupId");
  const stdName = params.get("std") || "10";
  const activeKey = stdName === "12" ? "twelfth" : "tenth";

  const user = Shell.init({ allowedRoles: ["HEADMASTER"], activeKey, title: "Section Detail" });
  if (!user) return;

  Utils.qs("#crumb-std").textContent = stdName === "12" ? "12th Standard" : "10th Standard";
  Utils.qs("#crumb-std").href = `standard.html?std=${stdName}`;
  Utils.qs("#view-students-btn").href = `students.html?scope=all&sectionGroupId=${sectionGroupId}`;

  let currentStandardId = null;

  loadOverview();

  async function loadOverview() {
    const statsRegion = Utils.qs("#section-stats");
    try {
      const [sectionGroup, students, assignments] = await Promise.all([
        api.get(`/api/sections/${sectionGroupId}`),
        api.get("/api/students", { sectionGroupId }),
        api.get("/api/class-teacher-assignments"),
      ]);
      currentStandardId = sectionGroup.standardId;

      Utils.qs("#section-title").textContent = `${sectionGroup.standardName} - ${sectionGroup.name}`;
      Utils.qs("#crumb-section").textContent = sectionGroup.name;

      const assignment = assignments.find((a) => a.sectionGroupId == sectionGroupId && a.active);

      statsRegion.innerHTML = `
        <div class="stat-card accent-primary">
          <div class="stat-label">Class Teacher</div>
          <div class="stat-value" style="font-size:18px;">${assignment ? Utils.escapeHtml(assignment.teacherName) : "Not assigned"}</div>
          <div class="stat-icon">\u{1F9D1}\u200D\u{1F3EB}</div>
        </div>
        <div class="stat-card accent-primary">
          <div class="stat-label">Students</div>
          <div class="stat-value">${students.length}</div>
          <div class="stat-icon">\u{1F464}</div>
        </div>
        <div class="stat-card accent-accent">
          <div class="stat-label">Section/Group</div>
          <div class="stat-value" style="font-size:18px;">${Utils.escapeHtml(sectionGroup.name)}</div>
          <div class="stat-icon">\u{1F3EB}</div>
        </div>`;

      loadSubjects();
      loadExams();
    } catch (err) {
      Utils.setError(statsRegion, err.message, loadOverview);
    }
  }

  // Every subject offered in this section/group, with the teacher(s) assigned to it.
  async function loadSubjects() {
    const region = Utils.qs("#subjects-region");
    try {
      const [subjects, assignments] = await Promise.all([
        api.get("/api/subjects", { sectionGroupId }),
        api.get("/api/subject-teacher-assignments"),
      ]);
      if (!subjects.length) {
        Utils.setEmpty(region, "No subjects are offered in this section/group yet.");
        return;
      }
      const mine = assignments.filter((a) => a.sectionGroupId == sectionGroupId && a.active);
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Subject</th><th>Subject Teacher</th></tr></thead>
            <tbody>
              ${subjects
                .map((s) => {
                  const teachers = mine.filter((a) => a.subjectId == s.id).map((a) => Utils.escapeHtml(a.teacherName));
                  return `<tr>
                    <td>${Utils.escapeHtml(s.name)}</td>
                    <td>${teachers.length ? teachers.join(", ") : '<span class="badge badge-warning">Not assigned</span>'}</td>
                  </tr>`;
                })
                .join("")}
            </tbody>
          </table>
        </div>`;
    } catch (err) {
      Utils.setError(region, err.message, loadSubjects);
    }
  }

  async function loadExams() {
    const select = Utils.qs("#exam-select");
    try {
      const exams = await AcademicApi.getExams(currentStandardId);
      if (!exams.length) {
        select.innerHTML = "<option>No exams available</option>";
        Utils.setEmpty(Utils.qs("#ranking-region"), "No exams have been created for this standard yet.");
        return;
      }
      select.innerHTML = exams.map((e) => `<option value="${e.id}">${Utils.escapeHtml(e.name)}</option>`).join("");
      select.value = exams[exams.length - 1].id;
      select.addEventListener("change", () => loadRanking(select.value));
      loadRanking(select.value);
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  async function loadRanking(examId) {
    const region = Utils.qs("#ranking-region");
    Utils.setLoading(region, "Loading ranking...");
    try {
      const [ranks, students] = await Promise.all([
        api.get(`/api/rankings/class/${sectionGroupId}`, { examId }),
        api.get("/api/students", { sectionGroupId }),
      ]);
      if (!ranks.length) {
        Utils.setEmpty(region, "No marks recorded for this exam yet.");
        return;
      }
      const studentMap = Object.fromEntries(students.map((s) => [s.id, s]));

      const attendancePcts = await Promise.all(
        ranks.map((r) =>
          api.get(`/api/class-attendance/student/${r.studentId}/percentage`).catch(() => ({ percentage: null }))
        )
      );

      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr>
              <th>Rank</th><th>Student</th><th>Register No</th><th>EMIS</th>
              <th>Attendance</th><th>Marks</th><th>Percentage</th>
            </tr></thead>
            <tbody>
              ${ranks
                .map((r, i) => {
                  const s = studentMap[r.studentId] || {};
                  const rankClass = r.rank <= 3 ? `rank-${r.rank}` : "";
                  return `
                  <tr>
                    <td><span class="rank-badge ${rankClass}">${r.rank}</span></td>
                    <td class="clickable" data-id="${r.studentId}">${Utils.escapeHtml(r.studentName)}</td>
                    <td>${Utils.escapeHtml(s.registerNumber || "-")}</td>
                    <td>${Utils.escapeHtml(s.emisNumber || "-")}</td>
                    <td>${attendancePcts[i].percentage != null ? attendancePcts[i].percentage + "%" : "-"}</td>
                    <td>${r.totalMarks} / ${r.totalMaxMarks}</td>
                    <td><strong>${r.percentage}%</strong></td>
                  </tr>`;
                })
                .join("")}
            </tbody>
          </table>
        </div>`;
      Utils.qsa("td.clickable", region).forEach((td) => {
        td.addEventListener("click", () => (location.href = `student-profile.html?id=${td.dataset.id}`));
      });
    } catch (err) {
      Utils.setError(region, err.message, () => loadRanking(examId));
    }
  }
})();
