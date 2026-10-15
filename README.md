# ACME College Records System (Jakarta EE)

A college records system built with Jakarta EE. It has two parts:

- A secured REST API for students, courses, professors, student clubs and course registrations.
- A single-page admin web app that runs on top of that API.

This was the final project for **CST8277 Enterprise Application Programming** at Algonquin College (Summer 2026). It was built by Group 05: **Renato Perocchio** and **Scott van Dijk**. We split the work equally, and both of us worked on every part.

## What's in this repository

| Folder | What it is |
|---|---|
| `REST-ACMECollege-Skeleton/` | The back end. It maps the ACME College MySQL database to JPA entities, has an EJB service layer, and exposes a secured JAX-RS REST API at `/api/v1`. |
| `JSF-ACMECollege-Skeleton/` | The front end. It's a single-page JSF admin app with a login page and 8 management panels. It calls the REST API through the JAX-RS client. |

The folder names come from the course's starter projects. They also set each app's URL (for example `http://localhost:8080/REST-ACMECollege-Skeleton/api/v1`), so they were kept as-is.

## Highlights

**Access control by role and by ownership**
- Logins are checked against the database by a custom Jakarta Security identity store, and passwords are stored as PBKDF2 hashes.
- An administrator (`ADMIN_ROLE`) can read and change everything.
- A regular user (`USER_ROLE`) can view only their own student record and their own course registrations. Admin-only actions are refused.

**35 JUnit tests against the running server**
- `TestACMECollegeSystem` calls the deployed API with the JAX-RS client.
- It covers create, read, update and delete for every resource.
- It also covers the access rules, plus negative cases: a regular user being refused admin-only actions, missing records returning 404, and responses coming back as JSON.

**JPA mapping**
- A composite key for course registrations (`@EmbeddedId` with `@MapsId`).
- Single-table inheritance for student clubs (academic and non-academic).
- `JOIN FETCH` queries that fixed a lazy-loading error when serializing a user's own registrations.

**Bugs caught along the way**
- A permission rule that blocked a regular user from reading data they were allowed to see.
- A cascade setting that deleted a student whenever one of their course registrations was deleted.

**Front end**
- Management panels for students, courses, professors, student clubs, course registrations, assigning professors, assigning grades, and club membership.
- All interface text lives in `Bundle.properties`.

## Tech

Java 21 · Jakarta EE 9 (JAX-RS, EJB, JPA, JSF, Jakarta Security) · Payara Server · MySQL 8 · Maven · JUnit 5 · Eclipse

## Run it

1. **Database.** In MySQL, create the `cst8277` user that the connection pool in `REST-ACMECollege-Skeleton/src/main/webapp/WEB-INF/payara-resources.xml` expects. The comments at the top of `acmecollege-create.sql` list the privileges it needs. Then run, in order:
   - `REST-ACMECollege-Skeleton/src/main/resources/sql/acmecollege-create.sql`
   - `REST-ACMECollege-Skeleton/src/main/resources/sql/acmecollege-data.sql`
2. **Back end.** Build `REST-ACMECollege-Skeleton` with Maven and deploy it to Payara Server on port 8080 (or use Run on Server in Eclipse). The API is then at `http://localhost:8080/REST-ACMECollege-Skeleton/api/v1`.
3. **Tests.** With the back end running, run `mvn test` in `REST-ACMECollege-Skeleton`.
4. **Front end.** Build and deploy `JSF-ACMECollege-Skeleton` the same way, then open it in a browser and sign in.
