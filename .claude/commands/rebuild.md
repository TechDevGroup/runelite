# /rebuild Command

## Description
Quick rebuild and restart cycle for RuneLite client development. Compiles the client, kills running instances, and launches the new version with debug flags.

## Usage
```
/rebuild
```

## What it does
1. Builds RuneLite client using Gradle: `:client:shadowJar -x test --build-cache`
2. Terminates any running Java processes (existing client instances)
3. Launches the newly built client with `--debug` flag

## When to use
- After making code changes to the RuneLite client
- Want to test changes immediately without manual restart
- Need to iterate quickly during development

## Script location
`.claude/command-automation-skill/scripts/rebuild.sh`

## Implementation
```bash
bash .claude/command-automation-skill/scripts/rebuild.sh
```

## Build time
Typically 20-30 seconds for incremental builds (with Gradle cache)

## Related commands
- `/quick-build` - Fast build without quality checks
- `/full-build` - Complete build with all validations
- `/validate` - Run code quality checks only
