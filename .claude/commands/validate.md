# /validate Command

## Description
Run code quality checks and tests to ensure code meets project standards.

## Usage
```
/validate
```

## What it does
Runs multiple validation tasks:
1. `checkstyleMain` - Code style compliance
2. `pmdMain` - Static code analysis
3. `test` - Unit and integration tests

## When to use
- Before committing code
- After significant refactoring
- To ensure code quality standards are met
- Pre-pull request validation

## Implementation
```bash
cd runelite
../gradlew.bat :runelite-client:checkstyleMain --console=plain
../gradlew.bat :runelite-client:pmdMain --console=plain
../gradlew.bat :runelite-client:test --console=plain
```

## Build time
Can take 1-2 minutes depending on number of tests

## Related commands
- `/build` - Quick compilation check
- `/rebuild` - Build and restart
- `/full-build` - Complete build with all checks
