(function () {
  const user = Shell.init({ allowedRoles: ["HEADMASTER"], activeKey: "rankings", title: "Rankings" });
  if (!user) return;

  const stdSelect = Utils.qs("#std-select");
  const sectionSelect = Utils.qs("#section-select");
  const examSelect = Utils.qs("#exam-select");

  init();
  stdSelect.addEventListener("change", async () => { await populateSections(); await populateExams(); loadRanking(); });
  sectionSelect.addEventListener("change", loadRanking);
  examSelect.addEventListener("change", loadRanking);

  async function init() {
    try {
      const standards = await AcademicApi.getStandards();
      if (!standards.length) {
        Utils.setEmpty(Utils.qs("#ranking-region"), "No standards have been set up yet.");
        return;
      }
      stdSelect.innerHTML = standards.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("");
      await populateSections();
      await populateExams();
      loadRanking();
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  async function populateSections() {
    const sections = await AcademicApi.getSections(stdSelect.value);
    sectionSelect.innerHTML = sections.map((sg) => `<option value="${sg.id}">${Utils.escapeHtml(sg.name)}</option>`).join("") || "<option value=''>No sections</option>";
  }

  async function populateExams() {
    const exams = await AcademicApi.getExams(stdSelect.value);
    examSelect.innerHTML = exams.map((e) => `<option value="${e.id}">${Utils.escapeHtml(e.name)}</option>`).join("") || "<option value=''>No exams</option>";
  }

  async function loadRanking() {
    const region = Utils.qs("#ranking-region");
    const sectionGroupId = sectionSelect.value;
    const examId = examSelect.value;
    if (!sectionGroupId || !examId) { Utils.setEmpty(region, "Select a section/group and exam to view rankings."); return; }
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
      region.innerHTML = `
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Rank</th><th>Student</th><th>Register No</th><th>EMIS</th><th>Marks</th><th>Percentage</th></tr></thead>
            <tbody>
              ${ranks
                .map((r) => {
                  const s = studentMap[r.studentId] || {};
                  const rankClass = r.rank <= 3 ? `rank-${r.rank}` : "";
                  return `<tr>
                    <td><span class="rank-badge ${rankClass}">${r.rank}</span></td>
                    <td class="clickable" data-id="${r.studentId}">${Utils.escapeHtml(r.studentName)}</td>
                    <td>${Utils.escapeHtml(s.registerNumber || "-")}</td>
                    <td>${Utils.escapeHtml(s.emisNumber || "-")}</td>
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
      Utils.setError(region, err.message, loadRanking);
    }
  }
})();
