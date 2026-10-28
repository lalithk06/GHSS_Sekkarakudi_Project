(function () {
  const user = Shell.init({ allowedRoles: ["SUBJECT_TEACHER", "CLASS_TEACHER"], activeKey: "marks", title: "Marks Entry" });
  if (!user) return;

  const params = new URLSearchParams(location.search);
  const classSelect = Utils.qs("#class-select");
  const examSelect = Utils.qs("#exam-select");
  const maxMarksInput = Utils.qs("#max-marks-input");

  let assignments = [];
  let students = [];
  let existingMarks = [];

  init();

  async function init() {
    const region = Utils.qs("#marks-region");
    try {
      const dash = await api.get("/api/dashboard/subject-teacher");
      assignments = dash.assignments || [];
      if (!assignments.length) {
        Utils.setEmpty(region, "You have no class/subject assignments yet. Contact your administrator.");
        classSelect.innerHTML = "<option>No assignments</option>";
        return;
      }
      classSelect.innerHTML = assignments
        .map(
          (a, i) =>
            `<option value="${i}">${Utils.escapeHtml(a.standardName)} - ${Utils.escapeHtml(a.sectionGroupName)} \u2014 ${Utils.escapeHtml(a.subjectName)}</option>`
        )
        .join("");

      const presetIdx = assignments.findIndex(
        (a) => String(a.sectionGroupId) === params.get("sectionGroupId") && String(a.subjectId) === params.get("subjectId")
      );
      classSelect.value = presetIdx >= 0 ? presetIdx : 0;

      classSelect.addEventListener("change", loadExamsForSelection);
      examSelect.addEventListener("change", loadMarksTable);
      maxMarksInput.addEventListener("change", renderTable);

      await loadExamsForSelection();
    } catch (err) {
      Utils.setError(region, err.message, init);
    }
  }

  function currentAssignment() {
    return assignments[Number(classSelect.value)];
  }

  async function loadExamsForSelection() {
    const a = currentAssignment();
    const sg = await api.get(`/api/sections/${a.sectionGroupId}`);
    const exams = await AcademicApi.getExams(sg.standardId);
    if (!exams.length) {
      examSelect.innerHTML = "<option>No exams</option>";
      Utils.setEmpty(Utils.qs("#marks-region"), "No exams have been created for this standard yet.");
      return;
    }
    examSelect.innerHTML = exams.map((e) => `<option value="${e.id}">${Utils.escapeHtml(e.name)}</option>`).join("");
    examSelect.value = exams[exams.length - 1].id;
    loadMarksTable();
  }

  async function loadMarksTable() {
    const region = Utils.qs("#marks-region");
    Utils.setLoading(region, "Loading students and marks...");
    const a = currentAssignment();
    try {
      const [studentList, marksForClass] = await Promise.all([
        api.get("/api/students", { sectionGroupId: a.sectionGroupId }),
        api.get(`/api/marks/class/${a.sectionGroupId}`, { examId: examSelect.value }),
      ]);
      students = studentList;
      existingMarks = marksForClass.filter((m) => String(m.subjectId) === String(a.subjectId));
      if (existingMarks.length) maxMarksInput.value = existingMarks[0].maxMarks;
      renderTable();
    } catch (err) {
      Utils.setError(region, err.message, loadMarksTable);
    }
  }

  function renderTable() {
    const region = Utils.qs("#marks-region");
    if (!students.length) {
      Utils.setEmpty(region, "No students found in this class yet.");
      return;
    }
    const markMap = Object.fromEntries(existingMarks.map((m) => [m.studentId, m]));

    region.innerHTML = `
      <div class="card">
        <div class="table-scroll">
          <table class="data-table">
            <thead><tr><th>Student</th><th>Register No</th><th>Marks</th><th>Max Marks</th></tr></thead>
            <tbody>
              ${students
                .map((s) => {
                  const existing = markMap[s.id];
                  return `
                  <tr>
                    <td>${Utils.escapeHtml(s.name)}</td>
                    <td>${Utils.escapeHtml(s.registerNumber || "-")}</td>
                    <td><input type="number" class="marks-input" data-student-id="${s.id}" data-mark-id="${existing ? existing.id : ""}"
                        min="0" max="${maxMarksInput.value}" value="${existing ? existing.marksObtained : ""}" /></td>
                    <td>${maxMarksInput.value}</td>
                  </tr>`;
                })
                .join("")}
            </tbody>
          </table>
        </div>
        <div class="form-actions">
          <button class="btn btn-primary" id="save-marks-btn">Save Marks</button>
        </div>
      </div>`;

    Utils.qs("#save-marks-btn").addEventListener("click", saveMarks);
  }

  async function saveMarks() {
    const region = Utils.qs("#marks-region");
    const maxMarks = Number(maxMarksInput.value) || 100;
    const inputs = Utils.qsa(".marks-input", region);

    let hasError = false;
    inputs.forEach((input) => {
      const val = input.value;
      if (val === "") return;
      const num = Number(val);
      if (isNaN(num) || num < 0 || num > maxMarks) {
        input.classList.add("input-error");
        hasError = true;
      } else {
        input.classList.remove("input-error");
      }
    });
    if (hasError) {
      Utils.toast("Marks must be between 0 and the max marks for every entered row.", "error");
      return;
    }

    const a = currentAssignment();
    const examId = Number(examSelect.value);
    const btn = Utils.qs("#save-marks-btn");
    Utils.setButtonLoading(btn, true, "Saving...");

    const toSave = inputs.filter((i) => i.value !== "");
    try {
      for (const input of toSave) {
        const payload = {
          studentId: Number(input.dataset.studentId),
          subjectId: a.subjectId,
          examId,
          marksObtained: Number(input.value),
          maxMarks,
        };
        if (input.dataset.markId) {
          await api.put(`/api/marks/${input.dataset.markId}`, payload);
        } else {
          await api.post("/api/marks", payload);
        }
      }
      Utils.toast("Marks saved successfully.", "success");
      loadMarksTable();
    } catch (err) {
      Utils.errorToast(err);
    } finally {
      Utils.setButtonLoading(btn, false);
    }
  }
})();
