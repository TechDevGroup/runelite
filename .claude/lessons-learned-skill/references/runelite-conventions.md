# RuneLite Coding Conventions

## Project Structure
- Build Tool: Gradle (Kotlin DSL)
- Languages: Java, Kotlin
- Java Version: 11
- Main Plugin Location: `runelite-client/src/main/java/net/runelite/client/plugins/`

## Plugin File Structure
Each plugin follows this conventional structure:
```
runelite-client/src/main/java/net/runelite/client/plugins/<pluginname>/
├── <PluginName>Plugin.java      # Main plugin class (required)
├── <PluginName>Config.java      # Configuration interface (optional)
├── <PluginName>Overlay.java     # Overlay rendering (optional)
└── <PluginName>Panel.java       # UI panel (optional)
```

## Required Plugin Components

### 1. Main Plugin Class
**Naming:** `{PluginName}Plugin.java`

**Required Elements:**
- Package: `net.runelite.client.plugins.<pluginname>`
- Extends: `net.runelite.client.plugins.Plugin`
- Annotations:
  - `@PluginDescriptor` (required) - defines name, description, tags
  - `@Singleton` (recommended)
  - `@Slf4j` (optional, for logging)

**Lifecycle Methods:**
- `startUp()` - Called when plugin is enabled
- `shutDown()` - Called when plugin is disabled

### 2. Configuration Interface (Optional)
**Naming:** `{PluginName}Config.java`

**Required Elements:**
- Package: Same as plugin
- Extends: `net.runelite.client.config.Config`
- Annotations:
  - `@ConfigGroup("<pluginname>")` on interface

**Config Items:**
- Use `@ConfigItem` annotation
- Define: keyName, name, description, position
- Methods return default values

### 3. Event Handling
**Pattern:**
- Inject `EventBus` using `@Inject`
- Register in `startUp()`: `eventBus.register(this)`
- Unregister in `shutDown()`: `eventBus.unregister(this)`
- Use `@Subscribe` on event handler methods

## Code Style Conventions

### Copyright Header
All files start with BSD-2 license header:
```java
/*
 * Copyright (c) YEAR, AUTHOR <email>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * ...
 */
```

### Import Organization
Order:
1. Java standard library imports
2. Third-party library imports (Google, Lombok, etc.)
3. RuneLite API imports
4. RuneLite client imports
5. Static imports

### Annotations
Common annotations used:
- `@Inject` - Dependency injection
- `@Provides` - Provide configuration instance
- `@Subscribe` - Event handlers
- `@Getter/@Setter` - Lombok accessors (use sparingly)
- `@Slf4j` - Lombok logging

### Field Declarations
Order:
1. Static final constants (all caps with underscores)
2. Injected dependencies
3. Private fields
4. Getters marked with `@Getter`

### Method Order
Convention:
1. `@Provides` methods
2. `startUp()` and `shutDown()`
3. Event handlers (`@Subscribe` methods)
4. Private helper methods

## Common Patterns

### Dependency Injection
```java
@Inject
private Client client;

@Inject
private ConfigManager configManager;

@Inject
private <PluginName>Config config;
```

### Configuration Provider
```java
@Provides
<PluginName>Config provideConfig(ConfigManager configManager)
{
    return configManager.getConfig(<PluginName>Config.class);
}
```

### Overlay Management
```java
// In startUp()
overlayManager.add(myOverlay);

// In shutDown()
overlayManager.remove(myOverlay);
```

### Event Bus Registration
```java
// In startUp()
eventBus.register(this);

// In shutDown()
eventBus.unregister(this);
```

### Event Handler
```java
@Subscribe
public void onGameTick(GameTick event)
{
    // Handle game tick
}
```

## Common Imports Reference

### Essential Plugin Imports
```java
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
```

### Configuration Imports
```java
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
```

### Event Handling Imports
```java
import net.runelite.client.eventbus.Subscribe;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GameStateChanged;
```

### Client API Imports
```java
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Skill;
```

## Naming Conventions

### Packages
- All lowercase
- Single word preferred (e.g., `boosts`, `agility`)
- Multi-word uses concatenation (e.g., `banktags`, `chatfilter`)

### Classes
- PascalCase
- Descriptive names
- Suffix indicates purpose:
  - `*Plugin` - Main plugin class
  - `*Config` - Configuration interface
  - `*Overlay` - Overlay rendering
  - `*Panel` - UI panel

### Methods
- camelCase
- Verb-based for actions (e.g., `updateBoostedStats()`)
- Boolean methods: `is*`, `has*`, `should*`

### Constants
- ALL_CAPS_WITH_UNDERSCORES
- `static final` fields

### Variables
- camelCase
- Descriptive names
- Avoid single-letter except in loops

## Build Configuration

### Plugin build.gradle (if needed)
Plugins typically don't need individual build.gradle files unless they have specific dependencies.

Common dependencies are provided by the main client module.

## Testing Patterns

Tests are located in parallel directory structure:
- Source: `src/main/java/...`
- Tests: `src/test/java/...`

## Common Mistakes to Avoid

1. Forgetting to register/unregister EventBus
2. Not handling null values from Client API
3. Missing @PluginDescriptor annotation
4. Wrong package structure (must be under net.runelite.client.plugins)
5. Not cleaning up resources in shutDown()
6. Forgetting @Provides method for Config
