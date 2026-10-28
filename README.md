# MiniBank — Java Banking Application
A Java-based banking application developed across 14 practicals. It demonstrates object-oriented programming, account validation, exception handling, repository-based account management, transaction processing, concurrency, persistence, reporting, and automated testing.

## Requirements

- Java 17 or later
- Apache Maven 3.9 or a compatible version

## Project Features

- Account creation and account management
- Deposits, withdrawals, and transfers
- Input validation and banking exception handling
- Object-oriented design using inheritance and interfaces
- Collections and repository-based account operations
- Concurrent transaction processing and synchronization
- Account data persistence
- Transaction statements and reporting
- Batch transaction processing
- Automated tests using JUnit

## Build and Test
Build the application and run the tests:

```
mvn clean package
```
The Maven lifecycle normally runs the test phase during packaging unless a build configuration explicitly skips tests.

Run the tests separately:

```
mvn test
```

## Run the Application
The application entry point is `service.MiniBank`, which is configured in the JAR manifest. The generated JAR name is `target/MiniBank-1.0.0.jar`.

Launch the interactive banking menu:

```
java -jar target\MiniBank-1.0.0.jar menu
```
Run the built-in demonstration mode:

```
java -jar target\MiniBank-1.0.0.jar
```

## Menu Features
The interactive menu supports the following actions:

1. Open Account
2. Deposit
3. Withdraw
4. Transfer
5. List Accounts
6. View Statement
7. Generate Report
8. Batch Transactions
9. Exit

## Persistence and Reports
The project stores account data in `data/accounts.dat` and uses the `logs/` directory for generated reports and transaction logs. These files and folders are created when the application performs the relevant operations.

Examples used by the implementation include:

- `data/accounts.dat`
- `logs/mini-bank-report.txt`
- `logs/end-of-day-report.txt`
- `logs/mini-bank-log.txt`

## Testing
The project includes JUnit tests for banking service operations.

```
mvn test
```

## Packaging
Maven packages the application into a runnable JAR using the project artifact ID and version from `pom.xml`.

The generated artifact is `target/MiniBank-1.0.0.jar`.

## Practical Progression
The project was developed across 14 practicals:

- Practicals 1–4: Banking fundamentals, entities, object methods, and input validation
- Practicals 5–8: Inheritance, interfaces, reflection, and exception handling
- Practicals 9–11: Concurrency, transaction processing, and persistence
- Practicals 12–14: Collections, repository operations, testing, and final system integration
