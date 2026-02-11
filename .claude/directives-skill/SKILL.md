---
name: directives-skill
description: Maintains project directives and provides a structured workflow for following them using numbered bullet points. Use this skill to review project directives, optimize token usage by avoiding redundant reads, and follow established workflows for RuneLite plugin development.
license: MIT
---

# Directives Skill

## Purpose
Maintains project directives and provides a structured workflow for following them using numbered bullet points.

## Current Directives

### 1. Token Optimization
**Avoid redundant file reads to minimize token I/O**

Actions:
1. Before reading any file, check if you have recently read or written it in the current context
2. If you have the information cached, use it directly without re-reading
3. Only re-read if context was summarized/truncated (you genuinely don't recall the contents)
4. Reference line numbers and offsets from memory when possible

### 2. Progressive Context Loading
**Load skill resources incrementally as needed**

Actions:
1. Identify which skill is needed for the current task
2. Load only the SKILL.md for that specific skill
3. Load additional bundled resources (examples/, scripts/, references/) only when required
4. Avoid loading all documentation upfront

### 3. RuneLite Development Workflow
**Follow established patterns when developing plugins**

Actions:
1. Work within the `./runelite/` directory for all plugin development
2. Reference existing plugins in `runelite-client/src/main/java/net/runelite/client/plugins/` for patterns
3. Follow Java/Kotlin conventions observed in the codebase
4. Use Gradle for building and testing
5. Consult `./.claude/lessons-learned-skill/` for error prevention guidance

### 4. Error Prevention First
**Consult lessons learned before implementing solutions**

Actions:
1. Before starting a task, check `./.claude/lessons-learned-skill/SKILL.md`
2. Review relevant error patterns and happy paths
3. Follow the recommended approach that avoids known issues
4. Update lessons learned when encountering new errors

### 5. Skill-First Command Execution
**Use skill workflows instead of direct command execution**

Actions:
1. Preface any command execution with: "I will [action] by using the [skill-name] workflow step [N]"
2. Check `./.claude/command-automation-skill/SKILL.md` for available command workflows
3. Execute the skill's script from `./.claude/[skill-name]/scripts/` directory
4. Only run commands directly if no skill workflow exists for that operation
5. Reference specific line numbers from skill files when citing workflows

**Examples:**
- Build operations: Use `command-automation-skill` → `/rebuild` workflow (lines 126-135)
- Validation: Use `command-automation-skill` → `/validate` workflow (lines 158-166)
- Git operations: Use `command-automation-skill` → `/commit` or `/sync` workflows (lines 181-207)

## Workflow Template

When starting a new task, follow this numbered workflow:

1. **Understand Requirements**
   - Read the task description
   - Identify what needs to be accomplished
   - Determine if this is familiar (use cached knowledge) or new (may need research)

2. **Check Directives**
   - Review relevant directives from this file
   - Check if token optimization applies (avoid re-reading recent files)
   - Identify which skills are needed

3. **Consult Lessons Learned**
   - Load `./.claude/lessons-learned-skill/SKILL.md` if needed
   - Review error patterns related to the task
   - Note the happy path approach

4. **Plan Execution**
   - Break down the task into steps
   - Identify required files/resources (use cached knowledge when possible)
   - Choose the approach least likely to encounter known errors

5. **Execute**
   - Follow the plan
   - Apply directives throughout (especially token optimization)
   - Update lessons learned if new errors are encountered

6. **Verify**
   - Confirm the task is complete
   - Document any new patterns or errors discovered

## Usage Examples

### Example 1: Creating a New Plugin
```
1. Check if I recall the plugin structure (avoid re-reading if I do)
2. Reference cached knowledge of plugin patterns
3. Check lessons-learned for common plugin creation errors
4. Create plugin following established patterns
5. Update lessons-learned if issues arise
```

### Example 2: Debugging Build Issues
```
1. Review error message
2. Check lessons-learned-skill for this specific error
3. If documented, follow the happy path solution
4. If new error, investigate and document solution
5. Update lessons-learned with the resolution
```

## Maintenance

This file should be updated when:
- New high-level directives are established
- Workflow improvements are identified
- Major project structure changes occur

For task-specific errors and solutions, update `./.claude/lessons-learned-skill/` instead.
