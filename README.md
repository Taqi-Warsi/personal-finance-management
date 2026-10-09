# Online Personal Finance Management Platform

A role-based personal finance system built with core Java, JDBC and MySQL. Users track
expenses and budgets, Financial Advisors give advice, and Admins manage the platform.

**Status: Review 1 (pre-midterm) complete.** The core Java layers are built and demoable.
Servlets, login/sessions and the dashboards are planned for Review 2.

## Tech stack
- Java 17, Maven
- MySQL 8 + JDBC (`mysql-connector-j`), raw `PreparedStatement` (no ORM)
- JUnit 5 (declared, tests to come)

## Setup
1. Install JDK 17, MySQL Server and an IDE (IntelliJ IDEA used here).
2. Run `schema.sql` in MySQL Workbench. It creates the `pfm_db` database and six tables.
3. In `src/main/resources`, create `db.properties` (see `db.properties.example`):
   ```properties
   db.url=jdbc:mysql://localhost:3306/pfm_db
   db.username=root
   db.password=YOUR_PASSWORD
   ```
4. Open the project in the IDE, let Maven download dependencies, then run `Main`.

`Main` is a demo runner. It creates its own temporary data and deletes it at the end, so it
is safe to run repeatedly.

## Project structure
```
com.pfm.model       Domain classes: AppUser (abstract) -> Admin, FinancialAdvisor, User;
                    Expense, Budget, Advice, Feedback, Interaction; Role enum;
                    Reportable and Notifiable interfaces
com.pfm.exception   InvalidBudgetException, InsufficientDataException,
                    UnauthorizedAccessException, DataAccessException
com.pfm.dao         Dao<T> interface + UserDao, ExpenseDao, BudgetDao,
                    AdviceDao, FeedbackDao, InteractionDao
com.pfm.service     ExpenseService (transactions), ReportService (collections/streams),
                    AccessControl (role permission checks)
com.pfm.thread      BudgetMonitor (background thread), SyncDemo (synchronization demo)
com.pfm.util        DBConnectionUtil (reads db.properties)
```
Layering rule: services call DAOs, DAOs call JDBC. No SQL exists outside the DAO classes.

## Review 1 rubric: where each item lives

| Rubric item | Marks | Where to look |
|---|---|---|
| **OOP** (inheritance, polymorphism, exceptions, interfaces) | 10 | **Inheritance:** `Admin`, `FinancialAdvisor`, `User` extend abstract `AppUser`. **Polymorphism:** each subclass overrides `getDashboardSummary()` and `getPermissions()`; `AccessControl.require()` relies on it. **Interfaces:** `Reportable`, `Notifiable`. **Exceptions:** four custom exceptions in `com.pfm.exception`; checked (`InvalidBudgetException`, `InsufficientDataException`) vs unchecked (`UnauthorizedAccessException`, `DataAccessException`). |
| **Collections & Generics** | 6 | Generic `Dao<T>` interface. `ReportService`: `Map<String, List<Expense>>`, `Map<String, BigDecimal>`, `Set<String>` (sorted `TreeSet`), `List<Budget>` sorted with a `Comparator`, streams and collectors. |
| **Multithreading & Synchronization** | 4 | `BudgetMonitor` runs on a `ScheduledExecutorService` and flags budgets at 80% or more. `SyncDemo` runs two threads on shared state, with and without `synchronized`. `ExpenseService` serializes each add-expense operation with a `synchronized` block. |
| **Classes for DB operations** | 7 | Model classes mirror the six tables. Six DAO classes implement full CRUD plus finder methods such as `findByUserId` and `findByStatus`. |
| **JDBC** | 3 | `DBConnectionUtil` (credentials read from a properties file, not hardcoded). Every query uses `PreparedStatement` with bound parameters, and try-with-resources closes every `Connection`, `Statement` and `ResultSet`. |
| **JDBC / transaction management** | 3 | `ExpenseService.addExpense()`: `setAutoCommit(false)`, then `commit()` or `rollback()`. Inserting an expense and updating its budget succeed or fail together. DAOs expose `Connection`-accepting overloads so both writes share one connection. |

## Design decisions
- **`BigDecimal` for money**, never `double`, because binary floating point cannot represent
  amounts like 0.10 exactly.
- **Overspending is blocked.** An expense that would push spending past the budget limit
  throws `InvalidBudgetException` and the transaction rolls back.
- **Passwords are stored as hashes.** `AppUser.toString()` deliberately omits the hash so it
  cannot leak into logs. (Real hashing is wired in with login, in Review 2.)
- **Synchronization, honestly stated:** the `spent_amount = spent_amount + ?` update is
  already atomic inside MySQL. The `synchronized` block serializes the whole
  insert-and-update operation within the application, and `SyncDemo` shows what goes wrong
  with unprotected shared in-memory state.
- **Exceptions are wrapped:** DAOs convert `SQLException` into an unchecked
  `DataAccessException`, so failures surface instead of being swallowed.

## Demo script (`Main`)
| Section | Shows |
|---|---|
| 1. Transaction success | Three expenses commit; the budget total updates to 750.00 |
| 2. Transaction rollback | A forced failure after the insert leaves row count and budget unchanged |
| 3. Overspend blocked | An expense over the limit is rejected and rolled back |
| 4. Collections and generics | Grouping, totals, top category, budgets sorted by usage |
| 5. Background monitor thread | `pool-1-thread-1` prints alerts for budgets at 80% or more |
| 6. Synchronization demo | Unsynchronized total is wrong; synchronized total is exactly 100000 |
| 7. Advice, feedback, interactions | Create, update and read through the three remaining DAOs |
| 8. Custom exceptions | `InsufficientDataException` and `UnauthorizedAccessException` triggered |

## Roadmap (Review 2)
- Servlets with `init`, `doGet`, `doPost`, `destroy`, and session handling
- Login with salted password hashing, and a filter for role-based access
- JSP dashboards for Admin, Financial Advisor and User
- Extra features: CSV export of reports, rule-based advice suggestions
