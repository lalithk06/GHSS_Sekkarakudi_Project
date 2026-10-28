/**
 * academic.js - shared lookups for standards / sections-groups / subjects
 * / exams. Several pages need these dropdowns, so they live here once.
 */

const AcademicApi = (() => {
  let standardsCache = null;

  async function getStandards(force = false) {
    if (!standardsCache || force) {
      standardsCache = await api.get("/api/standards");
    }
    return standardsCache;
  }

  // Looks a standard up by its number ("10" / "12"). Standards entered by hand
  // are often named "10th" or "Class 10", so fall back to comparing just the digits.
  async function getStandardByName(name) {
    const standards = await getStandards();
    const wanted = String(name).trim();
    const wantedDigits = wanted.replace(/\D/g, "");
    return (
      standards.find((s) => String(s.name).trim() === wanted) ||
      (wantedDigits ? standards.find((s) => String(s.name).replace(/\D/g, "") === wantedDigits) : undefined)
    );
  }

  function getSections(standardId) {
    return api.get("/api/sections", standardId ? { standardId } : undefined);
  }

  function getSubjects(standardId) {
    return api.get("/api/subjects", standardId ? { standardId } : undefined);
  }

  // Subjects actually offered for a specific section/group (via the
  // subject_section_groups mapping) - use this instead of getSubjects()
  // whenever a section/group is already selected (e.g. the Subject Teacher
  // Assignment form), so the dropdown never offers a subject/section
  // combination the backend would reject.
  function getSubjectsForSectionGroup(sectionGroupId) {
    return api.get("/api/subjects", sectionGroupId ? { sectionGroupId } : undefined);
  }

  function getExams(standardId) {
    return api.get("/api/exams", standardId ? { standardId } : undefined);
  }

  return { getStandards, getStandardByName, getSections, getSubjects, getSubjectsForSectionGroup, getExams };
})();
