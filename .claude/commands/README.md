# Claude Code Custom Commands

This directory contains custom slash command definitions for the RuneLight Spaces project.

## Available Commands

### Build & Development
- **`/rebuild`** - Quick rebuild and restart cycle (build + kill + launch)
- **`/build`** - Compile client classes only (fast verification)
- **`/validate`** - Run code quality checks and tests

## Using Commands

Commands can be invoked by typing the command name with a forward slash prefix in your conversation with Claude:

```
/rebuild
```

Claude will recognize the command and execute the corresponding workflow.

## Command Structure

Each command is documented in its own `.md` file with:
- Description of what the command does
- Usage instructions
- When to use it
- Implementation details
- Related commands

## Adding New Commands

To add a new command:

1. Create a new `.md` file in this directory (e.g., `mycommand.md`)
2. Document the command purpose, usage, and implementation
3. Add any required scripts to `.claude/command-automation-skill/scripts/`
4. Update this README with the new command

## Command Categories

### Quick Reference

| Command | Purpose | Time |
|---------|---------|------|
| `/build` | Compile only | ~10-20s |
| `/rebuild` | Build + restart | ~20-30s |
| `/validate` | Quality checks | ~1-2m |

## Related Resources

- **Automation Scripts**: `.claude/command-automation-skill/scripts/`
- **Skill Documentation**: `.claude/command-automation-skill/SKILL.md`
- **Directives**: `.claude/directives-skill/SKILL.md`
