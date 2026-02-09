---
name: command-automation-skill
description: Automates RuneLite build, rebuild, launch, and restart workflows. Consolidates repetitive Gradle build commands, client restart cycles, and development workflows into reusable scripts. Use this skill for all RuneLite compilation, testing, and client launch operations to save tokens and reduce errors.
license: MIT
---

# Command Automation Skill

## Purpose
Transform repetitive multi-step command sequences into single slash commands or skill invocations to reduce token usage and improve workflow efficiency.

## Core Principle
**DRY for Commands**: If you find yourself running the same sequence of commands repeatedly, consolidate them into a skill or script.

## Command Consolidation Patterns

### Pattern 1: Build and Restart
Common sequence when developing:
```bash
# Manual sequence (every time you make changes):
./gradlew :client:shadowJar -x test --build-cache
taskkill /F /IM java.exe
java -jar runelite-client/build/libs/client-*-shaded.jar --debug
```

**Consolidated as skill command:** `/rebuild`

### Pattern 2: Quick Build (No Tests)
```bash
# Manual:
./gradlew :client:shadowJar -x test -x javadoc -x checkstyleMain -x pmdMain --build-cache
```

**Consolidated as:** `/quick-build`

### Pattern 3: Test and Validate
```bash
# Manual:
./gradlew :client:test
./gradlew :client:checkstyleMain
./gradlew :client:pmdMain
```

**Consolidated as:** `/validate`

## Creating Command Skills

### Step 1: Identify Repetitive Sequences
Look for commands you run frequently:
- Build + restart cycles
- Test execution patterns
- Code quality checks
- Git workflows (add, commit, push)
- Environment setup

### Step 2: Create Skill Script

Create `./scripts/<command-name>.sh`:

```bash
#!/bin/bash
# Quick rebuild and restart

set -e  # Exit on error

JAVA_HOME=".jdk/jdk-11.0.30+7"
BUILD_DIR="runelite/runelite-client/build/libs"

echo "Building..."
cd runelite
./gradlew :client:shadowJar -x test --build-cache

if [ $? -eq 0 ]; then
    echo "Build successful, restarting client..."

    # Kill old client
    taskkill /F /IM java.exe 2>/dev/null || true

    sleep 2

    # Start new client
    cd ..
    $JAVA_HOME/bin/java -jar $BUILD_DIR/client-*-shaded.jar --debug &

    echo "Client restarted"
else
    echo "Build failed, not restarting"
    exit 1
fi
```

### Step 3: Document in SKILL.md

Add to this file:
```markdown
## /rebuild
Builds the client (skipping tests) and restarts if successful.

Usage: `/rebuild` or invoke command-automation-skill with "rebuild"

**What it does:**
1. Runs quick build (shadowJar, no tests)
2. Kills existing client process
3. Starts new client with --debug flag
4. Only restarts if build succeeds
```

## Available Commands

### /stop
**Purpose:** Stop running RuneLite client

**Sequence:**
1. `taskkill //F //IM java.exe`
2. Wait for cleanup

**When to use:**
- Client running in background mode
- Window exit button not responding
- Need to force stop client

**Script location:** `./scripts/stop-client.sh`

---

### /rebuild
**Purpose:** Quick rebuild and restart cycle

**Sequence:**
1. `./gradlew :client:shadowJar -x test --build-cache`
2. Kill java.exe processes (via /stop)
3. Start new client with --debug

**When to use:** After code changes, want to test immediately

---

### /quick-build
**Purpose:** Fast build with minimal checks

**Sequence:**
1. `./gradlew :client:shadowJar -x test -x javadoc -x checkstyleMain -x pmdMain --build-cache`

**When to use:** Rapid iteration, will validate later

---

### /full-build
**Purpose:** Complete build with all checks

**Sequence:**
1. `./gradlew :client:build --build-cache`

**When to use:** Before committing, want full validation

---

### /validate
**Purpose:** Run all code quality checks

**Sequence:**
1. `./gradlew :client:checkstyleMain`
2. `./gradlew :client:pmdMain`
3. `./gradlew :client:test`

**When to use:** Before committing, check code quality

---

### /clean-build
**Purpose:** Clean and rebuild from scratch

**Sequence:**
1. `./gradlew clean`
2. `./gradlew :client:shadowJar --build-cache`

**When to use:** Build issues, want fresh start

---

## Git Workflow Commands

### /commit
**Purpose:** Add, commit with message, and optionally push

**Sequence:**
1. `git status` (check state)
2. `git diff` (review changes)
3. `git add <files>`
4. `git commit -m "message\n\nCo-Authored-By: Claude Sonnet 4.5"`
5. Optional: `git push`

**When to use:** Standard commit workflow

---

### /sync
**Purpose:** Pull latest, resolve conflicts, push

**Sequence:**
1. `git fetch origin`
2. `git pull --rebase`
3. If conflicts: pause for resolution
4. `git push`

**When to use:** Sync with remote before starting work

---

## Development Workflow Commands

### /setup-hotreload
**Purpose:** Configure DCEVM hot reload environment

**Sequence:**
1. Check if DCEVM installed
2. Download if needed
3. Configure JVM args
4. Test hot reload

**When to use:** First-time hot reload setup

---

### /start-watch
**Purpose:** Start continuous build + auto-restart

**Sequence:**
1. Start `./gradlew :client:shadowJar --continuous` in background
2. Start file watcher script
3. Monitor for new JARs
4. Auto-restart client on successful build

**When to use:** Active development session, want auto-reload

---

## Usage in Code

### Invoking from Directive
Claude reads directives and checks for repetitive patterns:
```
If I run the same 3+ command sequence twice:
1. Check command-automation-skill for existing command
2. If exists, suggest using consolidated command
3. If not exists, offer to create new command skill
```

### Manual Invocation
User types: `/rebuild`

Claude:
1. Reads command-automation-skill
2. Finds `/rebuild` definition
3. Executes consolidated sequence
4. Reports success/failure

## Creating New Commands

### Template
```markdown
### /command-name
**Purpose:** Brief description

**Sequence:**
1. Step 1
2. Step 2
3. Step 3

**When to use:** Use case description

**Script location:** `./scripts/command-name.sh`
```

### Implementation
```bash
#!/bin/bash
# scripts/command-name.sh

set -e

# Your commands here
echo "Running command..."
```

## Best Practices

1. **Error Handling**: Always use `set -e` and check exit codes
2. **Idempotency**: Commands should be safe to run multiple times
3. **Status Messages**: Echo what's happening for user feedback
4. **Conditional Execution**: Only proceed if previous step succeeded
5. **Background Tasks**: Use `&` for processes that should continue
6. **Cleanup**: Kill old processes before starting new ones

## Token Savings

**Without automation:**
- Build command: ~150 tokens
- Kill command: ~100 tokens
- Start command: ~150 tokens
- **Total: ~400 tokens per cycle**

**With `/rebuild`:**
- Single command: ~50 tokens
- **Savings: ~350 tokens per cycle**

Over 10 rebuild cycles: **3,500 tokens saved**

## Maintenance

### When to Add New Commands
- Pattern used 3+ times
- Multi-step sequence (2+ commands)
- Complex flags/options
- Error-prone manual execution

### When to Update Commands
- Build process changes
- New flags added
- Paths change
- Error patterns emerge

## Integration with Other Skills

- **directives-skill**: Suggests using commands for patterns
- **dev-workflow-skill**: References commands for hot-reload
- **lessons-learned-skill**: Links commands to error resolutions

## Examples in Context

### Example 1: After Coding Changes
```
User: "I updated the UI component, can you test it?"

Claude: "I'll rebuild and restart the client using /rebuild"
*Executes: build + kill + restart sequence*
"Client restarted with your changes"
```

### Example 2: Before Committing
```
User: "Ready to commit"

Claude: "Running /validate to check code quality first"
*Executes: checkstyle + pmd + tests*
"All checks passed, proceeding with /commit"
*Executes: git workflow*
```

### Example 3: Starting Dev Session
```
User: "Starting work on feature"

Claude: "Setting up hot reload with /start-watch"
*Starts continuous build + file watcher*
"Auto-reload active, make your changes"
```

## Script Repository

All command scripts located in: `./.claude/command-automation-skill/scripts/`

**Available scripts:**
- `rebuild.sh` - Build and restart
- `quick-build.sh` - Fast build only
- `validate.sh` - Run all checks
- `start-watch.sh` - Continuous build + auto-restart
- `clean-build.sh` - Clean and rebuild

## Quick Reference

| Command | Purpose | Time Saved |
|---------|---------|------------|
| `/rebuild` | Build + restart | ~400 tokens |
| `/quick-build` | Fast build | ~150 tokens |
| `/validate` | All checks | ~300 tokens |
| `/commit` | Git commit workflow | ~500 tokens |
| `/start-watch` | Auto-reload setup | ~600 tokens |

**Total potential savings per session: 1,000-5,000 tokens**
