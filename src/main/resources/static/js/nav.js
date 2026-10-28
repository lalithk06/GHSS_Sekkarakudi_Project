/**
 * nav.js - builds the sidebar + topbar for every page based on the
 * logged-in user's role, so menu markup never has to be duplicated
 * across HTML files.
 */

const NAV_ITEMS = {
  HEADMASTER: [
    { key: "dashboard", label: "Dashboard", icon: "\u{1F3E0}", href: "hm-dashboard.html" },
    { key: "students", label: "Students", icon: "\u{1F464}", href: "students.html?scope=all" },
    { key: "tenth", label: "10th Standard", icon: "\u{1F4D8}", href: "standard.html?std=10" },
    { key: "twelfth", label: "12th Standard", icon: "\u{1F393}", href: "standard.html?std=12" },
    { key: "teachers", label: "Teachers", icon: "\u{1F9D1}\u200D\u{1F3EB}", href: "teachers.html" },
    { key: "rankings", label: "Rankings", icon: "\u{1F3C6}", href: "rankings.html" },
  ],
  ADMIN: [
    { key: "dashboard", label: "Dashboard", icon: "\u{1F3E0}", href: "admin-dashboard.html" },
    { key: "teachers", label: "Teachers", icon: "\u{1F9D1}\u200D\u{1F3EB}", href: "teachers.html" },
    { key: "assignments", label: "Assignments", icon: "\u{1F4CB}", href: "assignments.html" },
    { key: "academic", label: "Academic Setup", icon: "\u{1F3EB}", href: "academic-setup.html" },
    { key: "students", label: "Students", icon: "\u{1F464}", href: "students.html?scope=all" },
    { key: "addstudent", label: "Add Student", icon: "\u2795", href: "student-form.html" },
  ],
  CLASS_TEACHER: [
    { key: "dashboard", label: "Dashboard", icon: "\u{1F3E0}", href: "class-teacher-dashboard.html" },
    { key: "myclass", label: "My Class", icon: "\u{1F465}", href: "students.html?scope=myclass" },
    { key: "attendance", label: "Daily Attendance", icon: "\u2705", href: "attendance.html" },
    { key: "history", label: "Attendance History", icon: "\u{1F4C6}", href: "attendance-history.html" },
    { key: "subjects", label: "My Subject Classes", icon: "\u{1F4DA}", href: "subject-teacher-dashboard.html" },
    { key: "marks", label: "Marks", icon: "\u{1F4DD}", href: "marks.html" },
  ],
  SUBJECT_TEACHER: [
    { key: "dashboard", label: "Dashboard", icon: "\u{1F3E0}", href: "subject-teacher-dashboard.html" },
    { key: "marks", label: "Marks", icon: "\u{1F4DD}", href: "marks.html" },
  ],
};

const Shell = (() => {
  function sidebarHtml(role, activeKey) {
    const items = NAV_ITEMS[role] || [];
    const links = items
      .map(
        (item) => `
        <a href="${item.href}" class="${item.key === activeKey ? "active" : ""}">
          <span class="ic">${item.icon}</span><span>${item.label}</span>
        </a>`
      )
      .join("");
    return `
      <aside class="sidebar" id="app-sidebar">
        <div class="sidebar-brand">
          <div class="logo-mark">GH</div>
          <div class="brand-text">GHSS<small>Sekkarakudi</small></div>
        </div>
        <nav class="sidebar-nav">${links}</nav>
        <div class="sidebar-footer">
          <button class="btn logout-btn" id="logout-btn">\u{1F6AA}&nbsp; Logout</button>
        </div>
      </aside>
      <div class="sidebar-backdrop" id="sidebar-backdrop"></div>`;
  }

  function topbarHtml(user, title) {
    return `
      <header class="topbar">
        <div class="flex gap-14" style="align-items:center;">
          <button class="topbar-menu-btn" id="topbar-menu-btn" aria-label="Toggle navigation">\u2630</button>
          <div class="page-title">${title}</div>
        </div>
        <div class="user-block">
          <div class="user-meta">
            <div class="name">${Utils.escapeHtml(user.username)}</div>
            <div class="role">${Auth.roleLabel(user.role)}</div>
          </div>
          <div class="user-avatar">${Utils.initials(user.username)}</div>
        </div>
      </header>`;
  }

  /**
   * Initializes the page shell: enforces auth, renders sidebar/topbar,
   * and returns the logged-in user object.
   */
  function init({ allowedRoles, activeKey, title }) {
    const user = Auth.requireAuth(allowedRoles);
    if (!user) return null;

    document.title = title + " - GHSS Sekkarakudi";

    const shellRoot = document.getElementById("app-shell");
    shellRoot.insertAdjacentHTML("afterbegin", sidebarHtml(user.role, activeKey));

    const mainArea = document.createElement("div");
    mainArea.className = "main-area";
    const existingContent = document.getElementById("page-content");
    mainArea.insertAdjacentHTML("beforeend", topbarHtml(user, title));
    mainArea.appendChild(existingContent);
    shellRoot.appendChild(mainArea);

    document.getElementById("logout-btn").addEventListener("click", async () => {
      const ok = await Utils.confirmDialog({
        title: "Log out?",
        message: "You will need to sign in again to continue.",
        confirmText: "Log out",
      });
      if (ok) Auth.logout();
    });

    const sidebar = document.getElementById("app-sidebar");
    const backdrop = document.getElementById("sidebar-backdrop");
    const menuBtn = document.getElementById("topbar-menu-btn");
    menuBtn.addEventListener("click", () => {
      sidebar.classList.toggle("open");
      backdrop.classList.toggle("show");
    });
    backdrop.addEventListener("click", () => {
      sidebar.classList.remove("open");
      backdrop.classList.remove("show");
    });

    return user;
  }

  return { init };
})();
