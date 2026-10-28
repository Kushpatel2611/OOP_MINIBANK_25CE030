# MiniBank — Practical 14
A Java banking application developed across Practicals 1–14. It demonstrates object-oriented programming, account validation, exception handling, repository-based account management, transactions, concurrency, persistence, reporting, and automated testing.

## Requirements

- Java 17 or later
- Apache Maven 3.9 or compatible version

## Build and Test
Run these commands from the project root:

```
mvn clean package
mvn test
```

## Run the Application
Run the interactive banking menu:

```
java -jar target\MiniBank-1.0.0.jar menu
```
Run the demonstration mode:

```
java -jar target\MiniBank-1.0.0.jar
```

## Menu Features

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
Account data is stored in `data/accounts.dat`. Report files may be generated in the `logs/` directory.

## Testing
The project includes JUnit tests for banking service operations. Run `mvn test` to execute the test suite.

## Packaging
Maven creates the application JAR at `target/MiniBank-1.0.0.jar` after a successful package build.
