---
name: lessons-learned-skill
description: Consolidates errors, working solutions, and happy path guidance to prevent common mistakes in RuneLite plugin development. Use this skill before implementing solutions to avoid known pitfalls and follow proven approaches.
license: MIT
---

# Lessons Learned Skill

## Purpose
This skill maintains a knowledge base of:
- Common errors encountered during development
- Root causes and solutions
- Happy path approaches that avoid these errors
- Best practices specific to RuneLite plugin development

## How to Use This Skill

### Before Starting a Task
1. Review relevant sections below based on your task type
2. Follow the "Happy Path" approach to avoid known errors
3. If you encounter a new error, document it here for future reference

### After Encountering an Error
1. Document the error message and context
2. Record the root cause once identified
3. Document the working solution
4. Add a "Happy Path" entry to guide future work

## Error Categories

### 1. Build & Compilation Errors

#### Error: Gradle Build Fails - Missing Dependencies
**Symptoms:**
- Build fails with "Could not resolve dependency" messages
- Missing import statements cannot be resolved

**Root Cause:**
- Dependencies not properly declared in build.gradle
- Wrong Gradle module structure

**Solution:**
- Ensure plugin dependencies are declared in the correct build.gradle file
- Use `implementation` for dependencies needed at runtime
- Use `compileOnly` for dependencies provided by RuneLite core

**Happy Path:**
1. Check existing plugin build.gradle files for patterns
2. Add dependencies in the plugin's own build.gradle, not the root
3. Use versions consistent with other plugins
4. Run `./gradlew build` to verify

---

#### Error: Java Version Mismatch
**Symptoms:**
- "Unsupported class file major version" errors
- Build fails with version compatibility issues

**Root Cause:**
- Wrong Java version being used
- Gradle configured for different Java version

**Solution:**
- RuneLite requires Java 11
- Set JAVA_HOME to Java 11 installation
- Verify with `java -version`

**Happy Path:**
1. Check project's gradle.properties for required Java version
2. Use `./gradlew --version` to verify Gradle's Java version
3. Configure IDE to use Java 11 for the project

---

### 2. Plugin Development Errors

#### Error: Plugin Not Loading in RuneLite
**Symptoms:**
- Plugin compiles successfully but doesn't appear in RuneLite
- No error messages in console

**Root Cause:**
- Missing @PluginDescriptor annotation
- Plugin not in correct package structure
- Plugin class not extending Plugin

**Solution:**
- Ensure plugin class has @PluginDescriptor annotation
- Plugin must be in net.runelite.client.plugins.* package
- Plugin class must extend net.runelite.client.plugins.Plugin

**Happy Path:**
1. Create plugin in runelite-client/src/main/java/net/runelite/client/plugins/yourplugin/
2. Create YourPluginPlugin.java extending Plugin
3. Add @PluginDescriptor with name, description, tags
4. Use @Inject for dependencies
5. Implement startUp() and shutDown() methods

---

#### Error: Event Handlers Not Firing
**Symptoms:**
- @Subscribe annotated methods not being called
- Plugin loaded but not responding to game events

**Root Cause:**
- EventBus not properly registered
- Wrong event type being subscribed to
- Method signature incorrect

**Solution:**
- Ensure eventBus is injected: @Inject private EventBus eventBus;
- Register in startUp(): eventBus.register(this);
- Unregister in shutDown(): eventBus.unregister(this);
- Method must be annotated with @Subscribe and have correct parameter type

**Happy Path:**
1. Inject EventBus in plugin class
2. Register in startUp(), unregister in shutDown()
3. Use @Subscribe on handler methods
4. Check existing plugins for event type examples
5. Ensure method signature matches: `public void onEvent(EventType event)`

---

### 3. Git & Repository Errors

#### Error: Fork Creation Fails
**Symptoms:**
- `gh repo fork` fails with authentication error
- Permission denied when trying to fork

**Root Cause:**
- Not authenticated with gh cli
- Insufficient permissions

**Solution:**
- Run `gh auth login` to authenticate
- Verify authentication with `gh auth status`
- Ensure organization permissions if forking to org

**Happy Path:**
1. Check authentication: `gh auth status`
2. If not authenticated, run: `gh auth login`
3. Fork with: `gh repo fork OWNER/REPO --org ORG_NAME`
4. Verify fork was created: `gh repo view ORG_NAME/REPO`

---

### 4. UI Components & Overlay Development

#### Error: Interactive HTML Overlay Not Syncing with Overlay Bounds
**Symptoms:**
- HTML content doesn't resize with overlay
- Swing component injection causes disjointed behavior
- Alt-resize doesn't work properly with injected components
- Component position doesn't sync with overlay bounds

**Root Cause:**
- Attempting to inject Swing components outside the overlay rendering pipeline
- Trying to manually sync position every game tick (bespoke solution)
- Not using overlay bounds as the authoritative source of size
- Working against the overlay system instead of with it

**Solution:**
- Render Swing component directly to Graphics2D in renderExpanded()
- Use overlay bounds (from getBounds() or overlay system) as authoritative size
- Forward mouse events through InteractiveOverlay's onClicked() method
- Let the overlay system handle all positioning and sizing
- Remove manual component injection into canvas parent
- Remove game tick synchronization code

**Happy Path:**
1. Create HTMLOverlay extending InteractiveOverlay
2. In renderExpanded(), get size from overlay bounds: `getBounds().getSize()`
3. Sync Swing component size to match overlay bounds exactly
4. Render component directly: `contentContainer.paint(graphics)`
5. Forward click events in onClicked() using MouseEvent dispatch
6. Let overlay system handle Alt-resize, bounds indicators, and positioning
7. Never inject components outside the overlay rendering pipeline

**Example Implementation:**
```java
@Override
protected void renderExpanded(Graphics2D graphics)
{
    // Overlay bounds are authoritative
    Dimension size = getBounds() != null ? getBounds().getSize() : getConfig().getPreferredSize();

    // Sync Swing component to overlay size
    if (!contentContainer.getSize().equals(size))
    {
        contentContainer.setSize(size);
        contentContainer.doLayout();
    }

    // Render directly to overlay graphics
    contentContainer.paint(graphics);
}

@Override
protected boolean onClicked(Point point)
{
    // Forward events to Swing component
    MouseEvent mouseEvent = new MouseEvent(contentContainer,
        MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(),
        0, point.x, point.y, 1, false);
    contentContainer.dispatchEvent(mouseEvent);
    return true;
}
```

---

### 5. File Operations & Context Management

#### Error: Redundant File Reads (Token Waste)
**Symptoms:**
- Re-reading files that were just written or recently read
- High token usage for tasks

**Root Cause:**
- Not utilizing cached context from recent file operations
- Unnecessary verification reads

**Solution:**
- Reference information from recent reads/writes
- Only re-read if context was truncated/summarized
- Use line number offsets from memory

**Happy Path:**
1. Before using Read tool, check if file was recently read/written
2. If information is in recent context, use it directly
3. Only re-read if you genuinely don't recall contents
4. Reference files by path with line numbers: `path/to/file.java:42`

---

## RuneLite Specific Best Practices

### Plugin Structure
```
runelite-client/src/main/java/net/runelite/client/plugins/yourplugin/
├── YourPluginPlugin.java          # Main plugin class
├── YourPluginConfig.java          # Configuration interface
├── YourPluginPanel.java           # UI panel (if needed)
└── YourPluginOverlay.java         # Overlay rendering (if needed)
```

### Common Imports
```java
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
```

### Plugin Lifecycle
1. Constructor called (minimal initialization)
2. Dependencies injected (@Inject)
3. startUp() called when plugin enabled
4. shutDown() called when plugin disabled

### Configuration Pattern
- Create interface extending Config
- Use @Provides to provide config instance
- Annotate config methods with @ConfigItem
- Access via injected config instance

## Maintenance Notes

### When to Update This File
- After resolving any error not documented here
- When discovering a more efficient approach
- When RuneLite API changes affect documented patterns
- When build tool versions change

### Structure Guidelines
- Keep error entries concise but complete
- Always include: Symptoms, Root Cause, Solution, Happy Path
- Use code examples where helpful
- Link to reference files when applicable

## Reference Files

See `./references/` for:
- Example error logs
- Working configuration samples
- Build output examples

See `./examples/` for:
- Minimal working plugin example
- Common plugin patterns
- Testing templates
