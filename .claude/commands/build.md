# /build Command

## Description
Standard build command for compiling RuneLite client classes without creating a full JAR.

## Usage
```
/build
```

## What it does
Compiles Java source files for the RuneLite client module: `:runelite-client:classes`

## When to use
- Verifying code changes compile correctly
- Quick syntax/type checking
- Before committing changes
- Faster than full shadowJar build when you don't need to run the client

## Implementation
```bash
cd runelite && ../gradlew.bat :runelite-client:classes --console=plain
```

## Build time
Typically 10-20 seconds for incremental compilation

## Related commands
- `/rebuild` - Full build + restart cycle
- `/quick-build` - Build shadowJar without tests
- `/validate` - Run quality checks
