(function () {
  const params = new URLSearchParams(location.search);
  const stdName = params.get("std") || "10";
  const activeKey = stdName === "12" ? "twelfth" : "tenth";
  const title = stdName === "12" ? "12th Standard" : "10th Standard";

  const user = Shell.init({ allowedRoles: ["HEADMASTER"], activeKey, title });
  if (!user) return;

  Utils.qs("#page-title-h1").textContent = title;
  Utils.qs("#crumb-current").textContent = title;
  Utils.qs("#page-sub").textContent =
    stdName === "12" ? "Select a group to view class details, marks and rankings." : "Select a section to view class details, marks and rankings.";

  load();

  async function load() {
    const region = Utils.qs("#tiles-region");
    try {
      const standard = await AcademicApi.getStandardByName(stdName);
      if (!standard) {
        Utils.setEmpty(region, `Standard "${stdName}" was not found on the backend.`);
        return;
      }
      const sections = await AcademicApi.getSections(standard.id);
      if (!sections.length) {
        Utils.setEmpty(region, "No sections/groups have been created for this standard yet.");
        return;
      }
      region.innerHTML = `<div class="tile-grid">${sections
        .map(
          (sg) => `
          <div class="tile-card" data-id="${sg.id}">
            <div class="tile-icon">\u{1F3EB}</div>
            <div class="tile-label">${Utils.escapeHtml(sg.name)}</div>
            <div class="tile-sub">${sg.active ? "Active" : "Inactive"}</div>
          </div>`
        )
        .join("")}</div>`;
      Utils.qsa(".tile-card", region).forEach((el) => {
        el.addEventListener("click", () => {
          location.href = `section-detail.html?sectionGroupId=${el.dataset.id}&std=${stdName}`;
        });
      });
    } catch (err) {
      Utils.setError(region, err.message, load);
    }
  }
})();
