(function () {
  // Admin adds students (basic details) and can edit everything.
  // Class teacher opens an existing student to fill in / update the detailed fields.
  const preUser = Auth.getUser();
  const user = Shell.init({
    allowedRoles: ["ADMIN", "CLASS_TEACHER"],
    activeKey: preUser && preUser.role === "CLASS_TEACHER" ? "myclass" : "addstudent",
    title: "Student Form",
  });
  if (!user) return;

  const params = new URLSearchParams(location.search);
  const studentId = params.get("id");
  const isEdit = !!studentId;
  const form = Utils.qs("#student-form");
  const standardSelect = Utils.qs("#standard-select");
  const sectionSelect = Utils.qs("#section-select");

  const isClassTeacher = user.role === "CLASS_TEACHER";
  if (isClassTeacher && !isEdit) { location.href = "students.html?scope=myclass"; return; }

  // Basic details are entered by the Admin; the rest by the class teacher.
  const DETAIL_FIELDS = ["emisNumber", "aadhaarNumber", "photo", "fatherName", "motherName", "guardianName",
                         "parentPhone", "parentEmail", "address"];
  const BASIC_FIELDS = ["name", "admissionNumber", "registerNumber", "dob", "gender", "standardId", "sectionGroupId"];

  if (isEdit) {
    Utils.qs("#form-title").textContent = isClassTeacher ? "Student Details" : "Edit Student";
    Utils.qs("#crumb-current").textContent = isClassTeacher ? "Student Details" : "Edit Student";
  }
  if (isClassTeacher) {
    // basic details are read-only for the class teacher
    BASIC_FIELDS.forEach((n) => { if (form[n]) form[n].disabled = true; });
    Utils.qs(".page-header p").textContent =
      "Fill in or update the student's details and documents. Basic details (name, admission no., class) are managed by the Admin.";
  } else if (!isEdit) {
    // Admin adding a new student: only the basic details; the class teacher completes the rest.
    DETAIL_FIELDS.forEach((n) => { const g = form[n] && form[n].closest(".form-group"); if (g) g.classList.add("hidden"); });
    Utils.qsa(".form-row", form).forEach((row) => {
      const groups = Utils.qsa(".form-group", row);
      if (groups.length && groups.every((g) => g.classList.contains("hidden"))) {
        row.classList.add("hidden");
        const prev = row.previousElementSibling;
        if (prev && prev.classList.contains("form-section-title")) prev.classList.add("hidden");
      }
    });
    Utils.qs(".page-header p").textContent =
      "Enter the basic details. The class teacher will add the remaining details and documents.";
  }

  init();

  async function init() {
    try {
      const standards = await AcademicApi.getStandards();
      standardSelect.insertAdjacentHTML(
        "beforeend",
        standards.map((s) => `<option value="${s.id}">${Utils.escapeHtml(s.name)}</option>`).join("")
      );
      standardSelect.addEventListener("change", () => populateSections(standardSelect.value));

      if (isEdit) {
        const student = await api.get(`/api/students/${studentId}`);
        Object.entries({
          name: student.name,
          admissionNumber: student.admissionNumber,
          registerNumber: student.registerNumber,
          emisNumber: student.emisNumber,
          dob: student.dob,
          gender: student.gender,
          aadhaarNumber: student.aadhaarNumber,
          fatherName: student.fatherName,
          motherName: student.motherName,
          guardianName: student.guardianName,
          parentPhone: student.parentPhone,
          parentEmail: student.parentEmail,
          address: student.address,
        }).forEach(([k, v]) => { if (form[k]) form[k].value = v || ""; });

        standardSelect.value = student.standardId;
        await populateSections(student.standardId);
        sectionSelect.value = student.sectionGroupId;
      }
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  async function populateSections(standardId) {
    sectionSelect.innerHTML = '<option value="">Select section/group</option>';
    if (!standardId) return;
    try {
      const sections = await AcademicApi.getSections(standardId);
      sectionSelect.insertAdjacentHTML(
        "beforeend",
        sections.map((sg) => `<option value="${sg.id}">${Utils.escapeHtml(sg.name)}</option>`).join("")
      );
    } catch (err) {
      Utils.errorToast(err);
    }
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    Utils.clearFormErrors(form);
    Utils.qs("#form-alert").innerHTML = "";

    if (isClassTeacher) { await saveDetailsOnly(); return; }

    const valid = Utils.validateForm(form, {
      name: [Utils.validators.required],
      admissionNumber: [Utils.validators.required],
      dob: [Utils.validators.required],
      gender: [Utils.validators.required],
      standardId: [Utils.validators.required],
      sectionGroupId: [Utils.validators.required],
      parentPhone: [Utils.validators.phone],
      parentEmail: [Utils.validators.email],
    });
    if (!valid) {
      Utils.qs("#form-alert").innerHTML = '<div class="alert alert-error">Please fix the highlighted fields.</div>';
      return;
    }

    const payload = {
      admissionNumber: form.admissionNumber.value.trim(),
      registerNumber: form.registerNumber.value.trim() || null,
      emisNumber: form.emisNumber.value.trim() || null,
      name: form.name.value.trim(),
      dob: form.dob.value,
      gender: form.gender.value,
      aadhaarNumber: form.aadhaarNumber.value.trim() || null,
      fatherName: form.fatherName.value.trim() || null,
      motherName: form.motherName.value.trim() || null,
      guardianName: form.guardianName.value.trim() || null,
      parentPhone: form.parentPhone.value.trim() || null,
      parentEmail: form.parentEmail.value.trim() || null,
      address: form.address.value.trim() || null,
      standardId: Number(form.standardId.value),
      sectionGroupId: Number(form.sectionGroupId.value),
    };

    const btn = Utils.qs("#save-btn");
    Utils.setButtonLoading(btn, true, "Saving...");
    try {
      let saved;
      if (isEdit) {
        saved = await api.put(`/api/students/${studentId}`, payload);
      } else {
        saved = await api.post("/api/students", payload);
      }

      const photoFile = form.photo.files[0];
      if (photoFile) {
        try {
          const fd = new FormData();
          fd.append("file", photoFile);
          await api.upload(`/api/students/${saved.id}/documents?documentType=STUDENT_PHOTO`, fd);
        } catch {
          Utils.toast("Student saved, but the photo upload failed. You can add it from the student's profile.", "warning");
        }
      }

      Utils.toast(isEdit ? "Student updated successfully." : "Student added successfully.", "success");
      location.href = `student-profile.html?id=${saved.id}`;
    } catch (err) {
      Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
    } finally {
      Utils.setButtonLoading(btn, false);
    }
  });

  // Class teacher: update only the detailed fields of this student.
  async function saveDetailsOnly() {
    const valid = Utils.validateForm(form, {
      parentPhone: [Utils.validators.phone],
      parentEmail: [Utils.validators.email],
    });
    if (!valid) {
      Utils.qs("#form-alert").innerHTML = '<div class="alert alert-error">Please fix the highlighted fields.</div>';
      return;
    }
    const payload = {
      emisNumber: form.emisNumber.value.trim() || null,
      aadhaarNumber: form.aadhaarNumber.value.trim() || null,
      fatherName: form.fatherName.value.trim() || null,
      motherName: form.motherName.value.trim() || null,
      guardianName: form.guardianName.value.trim() || null,
      parentPhone: form.parentPhone.value.trim() || null,
      parentEmail: form.parentEmail.value.trim() || null,
      address: form.address.value.trim() || null,
    };
    const btn = Utils.qs("#save-btn");
    Utils.setButtonLoading(btn, true, "Saving...");
    try {
      const saved = await api.put(`/api/students/${studentId}/details`, payload);
      const photoFile = form.photo.files[0];
      if (photoFile) {
        try {
          const fd = new FormData();
          fd.append("file", photoFile);
          await api.upload(`/api/students/${saved.id}/documents?documentType=STUDENT_PHOTO`, fd);
        } catch {
          Utils.toast("Details saved, but the photo upload failed. You can add it from the student's profile.", "warning");
        }
      }
      Utils.toast("Student details updated successfully.", "success");
      location.href = `student-profile.html?id=${saved.id}`;
    } catch (err) {
      Utils.qs("#form-alert").innerHTML = `<div class="alert alert-error">${Utils.escapeHtml(err.message)}</div>`;
    } finally {
      Utils.setButtonLoading(btn, false);
    }
  }
})();
