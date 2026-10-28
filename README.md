# School Student Management System — Backend

A REST API backend for a School Student Management System, built with Java, Spring
Boot, Spring Security (JWT), Spring Data JPA, and MySQL. This is **backend only** —
no HTML/CSS/JS. The frontend will be built separately once this API is verified.

## 1. Tech stack

- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security, Jakarta Validation
- MySQL (runtime), H2 (test profile only)
- JWT auth (jjwt), BCrypt password hashing
- Maven

## 2. Project structure

```
src/main/java/com/school/app/
├── auth/          # login endpoint, JWT issuing
├── security/      # JwtUtil, JwtAuthenticationFilter, CurrentUser
├── config/        # SecurityConfig, JPA auditing, DataInitializer (demo data)
├── user/          # User entity, Role enum, admin user management
├── teacher/       # Teacher entity + admin-managed CRUD
├── academic/      # Standard, SectionGroup, Class/Subject teacher assignments
├── subject/       # Subject entity + CRUD
├── student/       # Student entity, search, ownership-based authorization
├── document/      # StudentDocument + real multipart file storage
├── attendance/    # ClassAttendance - single daily attendance system, taken by the class teacher
├── marks/         # Exam + Mark entities, validation, duplicate prevention
├── ranking/       # On-the-fly rank calculation from Marks (tie-aware)
├── dashboard/     # Aggregated stats per role
├── exception/     # Global exception handling (@RestControllerAdvice)
└── common/        # BaseEntity (createdAt/updatedAt), ApiResponse envelope
```

Every response is wrapped in a consistent envelope:
```json
{ "success": true, "message": "OK", "data": { ... }, "timestamp": "..." }
```

## 3. Configure MySQL

Create a database (or let the app create it — the default JDBC URL uses
`createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE school_management;
```

Set these environment variables before running (defaults shown are for local dev
only — **never use these in production**):

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/school_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC` | JDBC URL |
| `DB_USERNAME` | `root` | MySQL user |
| `DB_PASSWORD` | `root` | MySQL password |
| `JWT_SECRET` | (long placeholder) | HMAC secret for signing JWTs — **override this** |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifetime |
| `FILE_UPLOAD_DIRECTORY` | `./uploads` | Where student documents are stored on disk |
| `DDL_AUTO` | `update` | Hibernate schema strategy (`update` for dev; consider Flyway for prod) |

Example (Linux/Mac):
```bash
export DB_URL="jdbc:mysql://localhost:3306/school_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC"
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=$(openssl rand -base64 48)
```

## 4. Build & run

```bash
mvn clean install       # downloads dependencies, compiles, runs tests
mvn spring-boot:run     # starts the API on http://localhost:8080
```

On startup the app does three things, all idempotent (safe to restart):

1. `DataInitializer` seeds the two built-in logins only: `headmaster` /
   `Headmaster@123` and `admin` / `Admin@123`. Standards, sections/groups,
   subjects, teachers and exams are **not** pre-built - the Admin creates
   them under *Academic Setup* and *Teachers*.
2. `LegacyDataRepair` finds subjects that exist in the database but have no
   row in `subject_section_groups` (inserted with SQL, or created before that
   table existed) and maps them to their section(s)/group(s), so they appear in
   the subject dropdowns. It never touches a subject the admin deliberately
   un-ticked.
3. `StartupDiagnostics` logs which database the app is connected to and how
   many rows it can see in each table (look for lines starting `DATA CHECK`).

**Change the demo passwords before using this anywhere but your own machine.**

## 5. Authentication

```
POST /api/auth/login
{ "username": "admin", "password": "Admin@123" }
```
Returns a JWT. Send it on every subsequent request:
```
Authorization: Bearer <token>
```

## 6. Authorization model

| Role | Access |
|---|---|
| HEADMASTER | Read-only across the whole school: students, teachers, marks, attendance, rankings, dashboards |
| ADMIN | Manage teachers, assignments, standards/sections/subjects, users |
| CLASS_TEACHER | Full student CRUD + documents + daily attendance, **scoped to their assigned section/group only** |
| SUBJECT_TEACHER | Marks only, **scoped to their assigned section/group + subject only** |

Ownership is enforced server-side (not just `@PreAuthorize` role checks) —
see `StudentService.assertClassTeacherOwnsSection`, `MarkService.assertOwnsSubjectClass`,
etc. A class teacher assigned to 10-A gets a 403 if they try to touch a 10-B student,
even if they build the request by hand.

## 7. Key API endpoints

Full controller list — see source for query params on GET endpoints.

```
POST   /api/auth/login

GET    /api/users                      (ADMIN)
POST   /api/users                      (ADMIN)
PUT    /api/users/{id}/status          (ADMIN)
DELETE /api/users/{id}                 (ADMIN)

GET    /api/teachers                   (HM, ADMIN)
POST   /api/teachers                   (ADMIN)
PUT    /api/teachers/{id}              (ADMIN)
PUT    /api/teachers/{id}/deactivate   (ADMIN)
DELETE /api/teachers/{id}              (ADMIN)

GET/POST/PUT/DELETE  /api/standards
GET/POST/PUT/DELETE  /api/sections?standardId=
GET/POST/PUT/DELETE  /api/subjects?standardId=

GET/POST   /api/class-teacher-assignments
DELETE     /api/class-teacher-assignments/{id}
GET/POST   /api/subject-teacher-assignments
DELETE     /api/subject-teacher-assignments/{id}

GET    /api/students?search=&registerNumber=&admissionNumber=&emisNumber=&standardId=&sectionGroupId=
GET    /api/students/{id}
POST   /api/students                   (ADMIN, CLASS_TEACHER)
PUT    /api/students/{id}              (ADMIN, CLASS_TEACHER)
DELETE /api/students/{id}              (ADMIN, CLASS_TEACHER)

POST   /api/students/{id}/documents    (multipart: file, documentType)
GET    /api/students/{id}/documents
GET    /api/documents/{id}             (downloads the file)
DELETE /api/documents/{id}

POST   /api/class-attendance
GET    /api/class-attendance?sectionGroupId=&date=
GET    /api/class-attendance/class/{sectionGroupId}?from=&to=
GET    /api/class-attendance/student/{studentId}
GET    /api/class-attendance/student/{studentId}/percentage

GET/POST/DELETE  /api/exams?standardId=
POST   /api/marks                      (SUBJECT_TEACHER, ADMIN)
PUT    /api/marks/{id}
GET    /api/marks/student/{studentId}
GET    /api/marks/class/{sectionGroupId}?examId=
GET    /api/marks/subject/{subjectId}?examId=
GET    /api/marks/exam/{examId}/results

GET    /api/rankings/class/{sectionGroupId}?examId=
GET    /api/rankings/standard/{standardId}?examId=

GET    /api/dashboard/hm               (HEADMASTER)
GET    /api/dashboard/admin            (ADMIN)
GET    /api/dashboard/class-teacher    (CLASS_TEACHER)
GET    /api/dashboard/subject-teacher  (SUBJECT_TEACHER)
```

## 8. curl walkthrough

```bash
BASE=http://localhost:8080

# 1. Login
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}' | jq -r '.data.token')

# 2. Call an authenticated endpoint
curl -s $BASE/api/standards -H "Authorization: Bearer $TOKEN"

# 3. Create a teacher (class teacher for 10-A)
curl -s -X POST $BASE/api/teachers \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"username":"ct_a","password":"Passw0rd!","role":"CLASS_TEACHER","fullName":"Kavitha R"}'

# 4. Assign them to 10-A (look up sectionGroupId from GET /api/sections?standardId=1)
curl -s -X POST $BASE/api/class-teacher-assignments \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"teacherId":1,"sectionGroupId":1}'

# 5. Log in as the class teacher, create a student
CT_TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ct_a","password":"Passw0rd!"}' | jq -r '.data.token')

curl -s -X POST $BASE/api/students \
  -H "Authorization: Bearer $CT_TOKEN" -H "Content-Type: application/json" \
  -d '{"admissionNumber":"ADM001","name":"Arun Kumar","dob":"2010-06-15","gender":"MALE","standardId":1,"sectionGroupId":1}'

# 6. Upload a document for that student
curl -s -X POST "$BASE/api/students/1/documents?documentType=STUDENT_PHOTO" \
  -H "Authorization: Bearer $CT_TOKEN" \
  -F "file=@photo.jpg"

# 7. Submit first-period attendance
curl -s -X POST $BASE/api/class-attendance \
  -H "Authorization: Bearer $CT_TOKEN" -H "Content-Type: application/json" \
  -d '{"sectionGroupId":1,"date":"2026-09-13","entries":[{"studentId":1,"status":"PRESENT"}]}'

# 8. Enter marks (as a subject teacher assigned to this section+subject)
curl -s -X POST $BASE/api/marks \
  -H "Authorization: Bearer $SUBJECT_TEACHER_TOKEN" -H "Content-Type: application/json" \
  -d '{"studentId":1,"subjectId":1,"examId":1,"marksObtained":85,"maxMarks":100}'

# 9. Retrieve ranking
curl -s "$BASE/api/rankings/class/1?examId=1" -H "Authorization: Bearer $TOKEN"
```

A ready-to-import Postman collection covering the same flow is at
`postman/school-management.postman_collection.json`.

## 9. Testing

```bash
mvn test
```

Covers: login success/failure, missing-token rejection, class-teacher
cross-section authorization (403), marks range validation, duplicate-marks
prevention, duplicate-attendance prevention, and tie-aware ranking. Uses
an in-memory H2 database (`application-test.yml`) so no MySQL is needed to
run the suite.

> Note: this sandbox has no outbound network access to Maven Central, so the
> build could not be compiled/run here. The code has been reviewed by hand
> (package/import consistency, brace balance, entity relationships) but you
> should run `mvn clean install` locally as the first step — that's the real
> compiler check.

## 10. Security notes / what's enforced server-side

- Passwords: BCrypt only, never plaintext.
- JWT: HMAC-SHA256, stateless sessions, invalid/expired tokens → 401 via a
  dedicated `AuthenticationEntryPoint` (never a stack trace).
- File uploads: content-type allow-list, size cap, filename is replaced with
  a random UUID before touching the filesystem (original name is kept only
  as metadata), path traversal guarded against.
- Ownership checks happen in the **service layer**, not just annotations —
  a class teacher cannot read/write another section's students by crafting
  requests manually, and the same pattern applies to subject teachers and
  class attendance and marks.
- `@RestControllerAdvice` catches not-found/duplicate/forbidden/validation/
  file-upload/DB-constraint errors and returns clean JSON — no stack traces
  leak to the client.

## 11. What's intentionally NOT here yet

Per your instructions, no HTML/CSS/JS/React — this phase is backend-only.
The API is designed so a future frontend just needs a base URL and a bearer
token.


## 10. Troubleshooting: "the data is in MySQL but the dropdowns are empty"

Log in as `admin` and look at the **Data check** card on the Admin dashboard
(or the `DATA CHECK` lines in the console at startup). It shows the database
the app is really connected to, the row counts it sees, and a plain-English
explanation of anything that would leave a dropdown empty. The usual causes:

| Symptom | Cause | Fix |
|---|---|---|
| Every dropdown empty, Data check shows 0 standards | App is connected to a **different database** than the one you are looking at (`createDatabaseIfNotExist=true` silently creates a blank `school_management`) | Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` to the right database and restart |
| Subject dropdown empty, other dropdowns fine | Subject has no row in `subject_section_groups` | Restart (auto-repaired), or Academic Setup -> Subjects -> Edit and tick the section(s)/group(s) |
| Teacher dropdown says "No active ... teachers" | `teachers.active = 0` | Set `active = 1` for the teachers to use |
| Teachers missing from a list; warning toast about skipped records | A teacher row points at a deleted user, or `users.role` is not exactly `CLASS_TEACHER` / `SUBJECT_TEACHER` | Correct the row (see the `WARN` line with the row id in the server log) |
| Exam dropdown empty | No rows in `exams` for that standard | Academic Setup -> Exams |

`GET /api/dashboard/data-health` (ADMIN / HEADMASTER) returns the same report as JSON.
