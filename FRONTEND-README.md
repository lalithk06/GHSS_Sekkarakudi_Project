# School Student Management System - Frontend

A functional HTML5 / CSS3 / vanilla JavaScript frontend for the Spring
Boot + MySQL backend. No frameworks, no build step. It is now embedded
directly in this backend project at `src/main/resources/static/`, so
Spring Boot serves it itself - one app, one port, no separate frontend
server and no cross-origin requests to worry about.

## 1. Folder structure

```
school-backend/
├── src/main/java/...                    Backend (unchanged except the 2 patches below)
└── src/main/resources/
    ├── application.yml
    └── static/                           <- the frontend lives here now
        ├── index.html                     Redirects to login or the right dashboard
        ├── css/
        │   ├── style.css                   Design system: variables, layout, buttons, cards, badges, alerts, modals, toasts
        │   ├── login.css                   Login page only
        │   ├── dashboard.css               Dashboard/profile-specific tweaks
        │   ├── tables.css                  Data table + pagination styles
        │   └── forms.css                   Form controls, validation, attendance toggle
        ├── js/
        │   ├── config.js                    Backend base URL (empty = same origin, see step 3)
        │   ├── api.js                        Central fetch wrapper (auth header, error handling, upload/download)
        │   ├── auth.js                        Login/logout/token/role redirects
        │   ├── utils.js                        Toasts, modals, confirm dialogs, validation, formatting
        │   ├── nav.js                           Role-based sidebar/topbar shell
        │   ├── academic.js                       Shared standards/sections/subjects/exams lookups
        │   └── <page>.js                          One file per page (see below)
        ├── pages/
        │   ├── login.html
        │   ├── hm-dashboard.html, standard.html, section-detail.html, rankings.html
        │   ├── admin-dashboard.html, teachers.html, assignments.html, academic-setup.html
        │   ├── class-teacher-dashboard.html, attendance.html, attendance-history.html
        │   ├── subject-teacher-dashboard.html, marks.html
        │   └── students.html, student-form.html, student-profile.html   (shared across roles)
        └── assets/images/
```

Every page follows the same shell pattern: a `<div id="app-shell">` wrapping
`<div id="page-content">`, plus `#toast-container` and `#modal-root`. The
page's own `<script>` calls `Shell.init({ allowedRoles, activeKey, title })`,
which enforces login, injects the sidebar/topbar for that role, and returns
the logged-in user object.

## 2. Running it

Nothing extra to install - it's part of the Spring Boot build. Just run the
backend as usual:

```bash
cd school-backend
mvn spring-boot:run
```

Then open **`http://localhost:8080`** in a browser (or whatever port
`server.port` is set to in `application.yml`). Spring Boot serves
`index.html` at the root automatically, which redirects to the login page.
No separate static file server, no `python -m http.server`, no CORS setup
needed - it's all one origin now.

## 3. Configuring the backend URL

Because the frontend is served by the same app it talks to,
`js/config.js` defaults to same-origin requests (`DEFAULT_API_BASE_URL =
""`), which works out of the box. You only need to change this if you
ever split the frontend out to a different host:

```js
const DEFAULT_API_BASE_URL = "http://your-server:8080";
```

Or at runtime from the browser console (persists in localStorage and
overrides the default):

```js
localStorage.setItem('sms_api_base_url', 'http://your-server:8080');
```

## 4. Backend changes made (and why)

The frontend was built strictly against the existing controllers - no new
endpoints were invented. Three small, necessary patches were made to the
backend source, already applied in this project:

1. **`SubjectTeacherDashboardDto` / `DashboardService`** - a subject teacher
   had no endpoint to discover their own `sectionGroupId`/`subjectId` pairs
   (the `/api/subject-teacher-assignments` list is ADMIN/HEADMASTER-only,
   and the dashboard only returned name strings like `"10th - A"`). The
   Marks and Subject Attendance screens need real IDs to load the right
   class/subject, so a `assignments: [{sectionGroupId, standardName,
   sectionGroupName, subjectId, subjectName}]` list was added to the DTO.
   `assignedSubjects` / `assignedClasses` (the original string lists) are
   unchanged, so nothing else breaks.

2. **`StudentDto`** - `aadhaarNumber` is accepted on create/update
   (`StudentRequest`) and stored on the `Student` entity, but the read DTO
   never returned it, so it could never be displayed back. Added the field
   + mapping line so the student profile page can show it.

3. **`SecurityConfig`** - previously *every* request except `/api/auth/**`
   and `/actuator/health` required a valid JWT. That's fine when the
   frontend is hosted separately, but now that Spring Boot serves the
   frontend itself, the browser needs to load `login.html`, the CSS and
   the JS *before* it has a token to send. Added `permitAll()` for `/`,
   `/index.html`, `/favicon.ico`, `/pages/**`, `/css/**`, `/js/**` and
   `/assets/**`. Every `/api/**` route keeps exactly the same
   authentication/authorization rules as before - only the static files
   are now reachable without a token.

No database schema or existing endpoint contracts were changed.

## 4b. Empty dropdowns - built-in diagnostics

The Admin dashboard has a **Data check** card (backed by
`GET /api/dashboard/data-health`) that shows which database the backend is
connected to, how many rows it sees, and what would leave a dropdown empty.
If the backend had to skip a record it could not read, `api.js` now shows a
visible warning toast (from the `X-Skipped-Records` response header) instead of
silently returning a shorter list. Standards named like `10th` / `Class 12` are
matched by their number on the Headmaster pages. See README section 10.

## 5. Known API/spec mismatches (not changed, just documented)

- **HM dashboard "Today's Attendance" / "Average Performance" cards**: the
  `/api/dashboard/hm` endpoint doesn't expose these figures (only student/
  teacher counts). The dashboard shows what's available; these two cards
  were left out rather than inventing numbers.
- **Student photo upload**: there's no `photoPath` field on
  `StudentRequest`/the update endpoint. Photos are instead uploaded as a
  `STUDENT_PHOTO` document via the existing document upload endpoint
  (`POST /api/students/{id}/documents?documentType=STUDENT_PHOTO`), and the
  profile page displays the most recent one. Functionally equivalent, just
  a different endpoint than a dedicated photo field would use.
- **Editing a teacher requires re-entering a password**: `PUT
  /api/teachers/{id}` reuses `CreateTeacherRequest`, which marks `password`
  `@NotBlank`. The edit form therefore always requires typing a password
  (existing or new) to save any change, even just a phone number update.
  Flagged in the form itself; not changed since it touches validation
  behaviour beyond a pure read-mapping fix.
- **No "activate" endpoint for teachers**: only `PUT /{id}/deactivate`
  exists (no reverse action), so the UI only offers a Deactivate button,
  never Reactivate.
- **No update endpoint for Exams**: `/api/exams` only supports create and
  delete (no `PUT`), so Academic Setup's Exams tab only offers Add/Delete,
  not Edit.
- **No activate/deactivate for Subjects or Sections/Groups**: only
  create/update/delete exist for `/api/subjects` and `/api/sections`, so
  Academic Setup only offers those three actions per spec's request for
  activate/deactivate toggles there.
- **Class attendance cannot be edited after submission** (by design -
  `ClassAttendanceService` throws a conflict on a duplicate date). The UI
  detects this and shows the already-submitted record read-only instead of
  letting the user retry. Subject-wise attendance was intentionally removed:
  subject teachers only enter marks, so there is a single attendance system,
  owned by the class teacher.

## 6. Test login credentials (from `DataInitializer`)

| Role | Username | Password |
|---|---|---|
| Headmaster | `headmaster` | `Headmaster@123` |
| Admin | `admin` | `Admin@123` |

Class Teacher / Subject Teacher accounts don't exist until the Admin
creates them from **Teachers -> Add Teacher**, and they only see data once
assigned to a class/subject from **Assignments**.

## 7. Suggested test flow

1. Log in as `admin` -> Academic Setup: confirm/add 10th (sections A-D) and
   12th (Bio-Maths, History, Maths-Computer, Bio-Computer), add subjects,
   add at least one Exam per standard (Academic Setup -> Exams tab) -> add
   a Class Teacher and a Subject Teacher under Teachers -> assign both
   under Assignments.
2. Log in as the new Class Teacher -> Add Student -> Daily Attendance
   (submit) -> Attendance History.
3. Log in as the new Subject Teacher -> Marks (enter marks for the exam)
   -> Subject Attendance.
4. Log in as `headmaster` -> browse 10th/12th -> section detail -> Rankings
   -> click a student to view the full profile (documents, marks,
   attendance tabs).
