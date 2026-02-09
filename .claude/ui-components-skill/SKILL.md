---
name: ui-components-skill
description: Guidelines and patterns for creating modular, reusable UI components in RuneLite plugins. Use this skill when building custom UI elements, panels, overlays, or interactive components. Emphasizes config-first approach, SOLID principles, and DRY code with fluent layout builders.
license: MIT
---

# UI Components Skill

## Purpose
Provides patterns, guidelines, and reusable components for building UI elements in RuneLite plugins following best practices.

## Core Principles

### 1. Config-First Approach
**ALL UI components must be driven by configuration, not hardcoded values.**

```java
// ❌ BAD: Hardcoded values
panel.setMaxLines(1000);
panel.setColor(Color.BLACK);

// ✅ GOOD: Config-driven
DevConsoleConfig config = DevConsoleConfig.builder()
    .maxLines(pluginConfig.consoleMaxLines())
    .backgroundColor(pluginConfig.consoleBackgroundColor())
    .build();
DevConsoleComponent console = new DevConsoleComponent(config);
```

### 2. SOLID Principles

#### Single Responsibility Principle (SRP)
Each component should have one reason to change.
- `DevConsoleComponent` - displays logs
- `DevConsoleConfig` - configuration data
- Plugin - coordinates components

#### Open/Closed Principle (OCP)
Open for extension, closed for modification.
- Use configuration to extend behavior
- Use interfaces for customization points

#### Liskov Substitution Principle (LSP)
Subtypes must be substitutable for their base types.
- Components implementing `ConfigurableComponent` must behave correctly

#### Interface Segregation Principle (ISP)
Keep interfaces focused and minimal.
- `ConfigurableComponent` - minimal required methods
- `ComponentConfig` - minimal configuration interface

#### Dependency Inversion Principle (DIP)
Depend on abstractions, not concrete implementations.
- Components depend on `ComponentConfig` interface
- Not on specific config implementations

### 3. Don't Repeat Yourself (DRY)
Extract common functionality into base classes and utilities.

## Component Architecture

### Base Interfaces

#### ConfigurableComponent<T>
```java
public interface ConfigurableComponent<T extends ComponentConfig>
{
    JComponent getComponent();
    void updateConfig(T config);
    T getConfig();
    void refresh();
    void dispose();
}
```

#### ComponentConfig
```java
public interface ComponentConfig
{
    boolean isValid();
    String getConfigKey();
}
```

### Abstract Base Class

#### AbstractConfigurableComponent<T>
Implements common lifecycle management:
- Lazy component initialization
- Thread-safe config updates
- EDT-aware refresh mechanism
- Resource cleanup

## Creating a New UI Component

### Step 1: Define Configuration

```java
@Value
@Builder
public class MyComponentConfig implements ComponentConfig
{
    @Builder.Default
    int maxItems = 100;

    @Builder.Default
    Color foregroundColor = Color.WHITE;

    @Override
    public String getConfigKey()
    {
        return "mycomponent";
    }

    @Override
    public boolean isValid()
    {
        return maxItems > 0;
    }
}
```

**Key Points:**
- Use `@Value` for immutability
- Use `@Builder` for flexible construction
- Provide sensible defaults with `@Builder.Default`
- Implement validation in `isValid()`

### Step 2: Implement Component

```java
public class MyComponent extends AbstractConfigurableComponent<MyComponentConfig>
{
    private JPanel panel;
    private JLabel label;

    public MyComponent(MyComponentConfig config)
    {
        super(config);
    }

    @Override
    protected JComponent buildComponent()
    {
        panel = new JPanel();
        label = new JLabel();
        label.setForeground(config.getForegroundColor());
        panel.add(label);
        return panel;
    }

    @Override
    protected void refreshInternal()
    {
        label.setForeground(config.getForegroundColor());
        label.setText("Items: " + config.getMaxItems());
    }
}
```

**Key Points:**
- Extend `AbstractConfigurableComponent`
- Build UI in `buildComponent()` (called once)
- Update UI in `refreshInternal()` (called on config changes)
- Access config via `this.config`

### Step 3: Use in Plugin

```java
public class MyPlugin extends Plugin
{
    @Inject
    private MyPluginConfig pluginConfig;

    private MyComponent myComponent;

    @Override
    protected void startUp()
    {
        MyComponentConfig componentConfig = MyComponentConfig.builder()
            .maxItems(pluginConfig.maxItems())
            .foregroundColor(pluginConfig.componentColor())
            .build();

        myComponent = new MyComponent(componentConfig);

        // Add to panel, overlay, etc.
        panel.add(myComponent.getComponent());
    }

    @Override
    protected void shutDown()
    {
        if (myComponent != null)
        {
            myComponent.dispose();
        }
    }
}
```

## Fluent Layout Builders

### VerticalBoxLayout
Common vertical stacking layout used in most RuneLite sidebar panels.

**Features:**
- Auto-gap between components
- Fluent API with chaining
- Separators and spacers
- Config-driven sizing

**Usage:**
```java
JPanel panel = VerticalBoxLayout.builder()
    .defaultGap(10)
    .autoGap(true)
    .build()
    .add(titleLabel)
    .addSeparator()
    .add(contentPanel)
    .addGlue()  // Push content apart
    .add(buttonPanel)
    .build();
```

**Builder Options:**
- `border(Border)` - Custom border
- `defaultGap(int)` - Gap size between components
- `autoGap(boolean)` - Auto-insert gaps
- `width(int)` - Fixed width

### GridPanelLayout
Grid-based layout for uniform component arrangements.

**Usage:**
```java
JPanel grid = GridPanelLayout.builder()
    .columns(3)
    .hgap(5)
    .vgap(5)
    .build()
    .add(component1)
    .add(component2)
    .add(component3)
    .build();
```

**Builder Options:**
- `rows(int)` - Number of rows (0 = dynamic)
- `columns(int)` - Number of columns
- `hgap(int)` - Horizontal gap
- `vgap(int)` - Vertical gap
- `border(Border)` - Custom border

## Fluent Component Builders

### UIComponents.label()
Creates labels with fluent API.

**Example:**
```java
JLabel title = UIComponents.label()
    .text(config.titleText())
    .bold()
    .alignCenter()
    .color(Color.WHITE)
    .build();

JLabel info = UIComponents.label()
    .text("Info text")
    .small()
    .alignLeft()
    .build();
```

**Methods:**
- `text(String)` - Label text
- `font(Font)` - Custom font
- `color(Color)` - Text color
- `bold()` - Use bold font
- `small()` - Use small font
- `alignLeft/Center/Right()` - Alignment
- `size(int, int)` - Fixed size

### UIComponents.button()
Creates buttons with actions.

**Example:**
```java
JButton saveBtn = UIComponents.button()
    .text("Save")
    .onClick(() -> save())
    .size(100, 30)
    .build();
```

**Methods:**
- `text(String)` - Button text
- `onClick(Runnable)` - Click handler
- `size(int, int)` - Button size
- `enabled(boolean)` - Enable/disable

### UIComponents.textField()
Creates text input fields.

**Example:**
```java
JTextField input = UIComponents.textField()
    .text(config.defaultValue())
    .placeholder("Enter value...")
    .columns(20)
    .build();
```

**Methods:**
- `text(String)` - Initial text
- `placeholder(String)` - Placeholder tooltip
- `columns(int)` - Column width
- `editable(boolean)` - Editable state

## Complete Example

### Building a Plugin Panel with Fluent API

```java
public class MyPluginPanel extends PluginPanel
{
    @Inject
    public MyPluginPanel(MyPluginConfig config)
    {
        super();
        setLayout(new BorderLayout());

        // Header section
        JPanel header = VerticalBoxLayout.create()
            .add(UIComponents.label()
                .text(config.panelTitle())
                .bold()
                .alignCenter()
                .build())
            .addSeparator()
            .build();

        // Content with grid
        JPanel content = GridPanelLayout.create(3)  // 3 columns
            .add(UIComponents.label().text("Stat 1").build())
            .add(UIComponents.label().text("Value 1").build())
            .add(UIComponents.button().text("Edit").build())
            .add(UIComponents.label().text("Stat 2").build())
            .add(UIComponents.label().text("Value 2").build())
            .add(UIComponents.button().text("Edit").build())
            .build();

        // Controls
        JPanel controls = VerticalBoxLayout.builder()
            .defaultGap(5)
            .build()
            .add(UIComponents.button()
                .text("Refresh")
                .onClick(this::refresh)
                .build())
            .add(UIComponents.button()
                .text("Clear")
                .onClick(this::clear)
                .build())
            .build();

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);
    }
}
```

## Layout Patterns

### Hiscores-Style Layout
```java
// Header with search
JPanel header = VerticalBoxLayout.create()
    .add(searchField)
    .addSeparator()
    .build();

// Grid of skills
JPanel skillsGrid = GridPanelLayout.builder()
    .columns(3)
    .hgap(5)
    .vgap(5)
    .build();

for (Skill skill : skills)
{
    skillsGrid.add(createSkillPanel(skill));
}

// Combine
add(header, BorderLayout.NORTH);
add(skillsGrid, BorderLayout.CENTER);
```

### Calculator-Style Layout
```java
// Display area
JPanel display = VerticalBoxLayout.create()
    .add(resultLabel)
    .build();

// Button grid
JPanel buttons = GridPanelLayout.create(4, 4)  // 4x4 grid
    .add(button1)
    .add(button2)
    // ... more buttons
    .build();

add(display, BorderLayout.NORTH);
add(buttons, BorderLayout.CENTER);
```

### Form-Style Layout
```java
JPanel form = VerticalBoxLayout.builder()
    .defaultGap(10)
    .build()
    .add(UIComponents.label().text("Name:").build())
    .add(UIComponents.textField().build())
    .addGap(5)
    .add(UIComponents.label().text("Value:").build())
    .add(UIComponents.textField().build())
    .addSeparator()
    .add(UIComponents.button().text("Submit").build())
    .build();
```

## Available Components

### DevConsoleComponent
In-app console for displaying logs with configurable appearance and behavior.

**Configuration:**
- `maxLines` - maximum log lines to keep
- `showTimestamp` - show/hide timestamps
- `showLogLevel` - show/hide log levels
- `autoscroll` - auto-scroll to latest
- `fontSize`, `fontFamily` - text appearance
- `backgroundColor`, `textColor` - colors
- `errorColor`, `warnColor`, `infoColor`, `debugColor` - log level colors

**Usage:**
```java
DevConsoleConfig config = DevConsoleConfig.builder()
    .maxLines(1000)
    .showTimestamp(true)
    .build();

DevConsoleComponent console = new DevConsoleComponent(config);
console.info("Information message");
console.error("Error message");
console.clear(); // Clear all logs
```

## Adding Sidebar Panels

### Step 1: Create Panel Class

```java
public class MyPluginPanel extends PluginPanel
{
    @Inject
    public MyPluginPanel(MyPluginConfig config)
    {
        super();
        setLayout(new BorderLayout());

        // Add components using config
        JLabel label = new JLabel(config.panelTitle());
        add(label, BorderLayout.NORTH);
    }
}
```

### Step 2: Register in Plugin

```java
@Inject
private ClientToolbar clientToolbar;

@Inject
private MyPluginPanel panel;

private NavigationButton navButton;

@Override
protected void startUp()
{
    navButton = NavigationButton.builder()
        .tooltip("My Plugin")
        .icon(ImageUtil.loadImageResource(getClass(), "/icon.png"))
        .priority(100)
        .panel(panel)
        .build();

    clientToolbar.addNavigation(navButton);
}

@Override
protected void shutDown()
{
    if (navButton != null)
    {
        clientToolbar.removeNavigation(navButton);
    }
}
```

## Threading Considerations

### EDT (Event Dispatch Thread)
All Swing component manipulation must occur on EDT:

```java
// ✅ GOOD: Using EDT
SwingUtilities.invokeLater(() -> {
    label.setText("New text");
});

// ❌ BAD: Not using EDT
label.setText("New text"); // From non-EDT thread
```

`AbstractConfigurableComponent` handles EDT automatically in `refresh()`.

## Component Lifecycle

1. **Construction** - Config validation
2. **Build** - Create Swing components (lazy, on first `getComponent()` call)
3. **Update** - Config changes trigger `refresh()`
4. **Dispose** - Clean up resources

## Best Practices

1. **Config-First Always**: No hardcoded values, everything from config
2. **Immutable Configs**: Use `@Value` and `@Builder`
3. **Validation**: Implement `isValid()` for all configs
4. **Thread Safety**: Use EDT for UI updates
5. **Resource Cleanup**: Implement `dispose()` for cleanup
6. **Single Responsibility**: One component, one purpose
7. **DRY**: Extract common patterns to base classes
8. **Testable**: Components should be testable in isolation

## Common Patterns

### Config Updates
```java
// Listen for config changes
@Subscribe
public void onConfigChanged(ConfigChanged event)
{
    if (event.getGroup().equals("myplugin"))
    {
        MyComponentConfig newConfig = MyComponentConfig.builder()
            .maxItems(pluginConfig.maxItems())
            .build();

        myComponent.updateConfig(newConfig);
    }
}
```

### Conditional Display
```java
if (pluginConfig.showComponent())
{
    panel.add(myComponent.getComponent());
}
else
{
    panel.remove(myComponent.getComponent());
}
panel.revalidate();
panel.repaint();
```

## Dockable Overlays

### Overview
Dockable overlays provide advanced canvas-based UI that appears on top of the game viewport, with collapsing, pinning, and docking capabilities.

### DockableOverlay Base Class
Extends `OverlayPanel` to provide:
- Multiple collapse modes (HIDDEN, COLLAPSED, MINIMIZED, EXPANDED)
- Pinning to keep overlay visible
- Config-driven appearance and behavior
- Movable, resizable, snappable positioning

### Collapse Modes

```java
public enum CollapseMode
{
    HIDDEN,      // Overlay completely hidden
    COLLAPSED,   // Only header bar visible
    MINIMIZED,   // Header + compact content
    EXPANDED     // Full content display
}
```

### Creating Dockable Overlays

**Step 1: Create Configuration**

```java
DockableOverlayConfig config = DockableOverlayConfig.builder()
    .title("My Overlay")
    .position(OverlayPosition.TOP_RIGHT)
    .preferredSize(new Dimension(250, 300))
    .initialCollapseMode(CollapseMode.EXPANDED)
    .collapsible(true)
    .pinnable(true)
    .movable(true)
    .resizable(true)
    .backgroundColor(ColorScheme.DARK_GRAY_COLOR)
    .priority(0.5f)
    .build();
```

**Step 2: Extend DockableOverlay**

```java
public class MyOverlay extends DockableOverlay
{
    public MyOverlay(Plugin plugin, DockableOverlayConfig config)
    {
        super(plugin, config);
    }

    @Override
    protected void renderExpanded(Graphics2D graphics)
    {
        // Render full content
        getPanelComponent().getChildren().add(
            LineComponent.builder()
                .left("Status:")
                .right("Active")
                .build()
        );
    }

    @Override
    protected void renderMinimized(Graphics2D graphics)
    {
        // Render compact content (optional)
        getPanelComponent().getChildren().add(
            LineComponent.builder()
                .left("Active")
                .build()
        );
    }
}
```

**Step 3: Register with OverlayManager**

```java
@Inject
private OverlayManager overlayManager;

private MyOverlay myOverlay;

@Override
protected void startUp()
{
    DockableOverlayConfig config = DockableOverlayConfig.builder()
        .title("My Overlay")
        .build();

    myOverlay = new MyOverlay(this, config);
    overlayManager.add(myOverlay);
}

@Override
protected void shutDown()
{
    if (myOverlay != null)
    {
        overlayManager.remove(myOverlay);
    }
}
```

### Overlay Controls

Overlays automatically include right-click menu entries:
- "Toggle Collapse" - Switch between COLLAPSED and EXPANDED
- "Toggle Pin" - Pin/unpin overlay
- "Hide" - Hide overlay completely

### Example: StopwatchOverlay

```java
public class StopwatchOverlay extends DockableOverlay
{
    private Instant startTime;
    private boolean running = false;

    public void start()
    {
        startTime = Instant.now();
        running = true;
    }

    @Override
    protected void renderExpanded(Graphics2D graphics)
    {
        Duration elapsed = Duration.between(startTime, Instant.now());
        String time = String.format("%d:%02d",
            elapsed.toMinutes(),
            elapsed.getSeconds() % 60);

        getPanelComponent().getChildren().add(
            LineComponent.builder()
                .left("Elapsed:")
                .right(time)
                .rightColor(running ? Color.GREEN : Color.WHITE)
                .build()
        );
    }
}
```

### Positioning

Use `OverlayPosition` enum for positioning:
- `TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`
- `BOTTOM_LEFT`, `BOTTOM_RIGHT`
- `ABOVE_CHATBOX_RIGHT`
- `CANVAS_TOP_RIGHT`
- `DYNAMIC` - Custom positioning

### Best Practices

1. **Config-Driven**: All overlay behavior from configuration
2. **Minimize Minimized**: Keep minimized view compact and essential
3. **Expand Expanded**: Show full details in expanded mode
4. **Right-Click Menus**: Use menu entries for controls
5. **Performance**: Avoid heavy rendering in overlay render loop

## Sidebar Overlay Controls

### OverlayControlPanel
Smart control panel that automatically generates UI controls for managing overlays from the sidebar.

**Features:**
- Visibility toggle (Show/Hide)
- Pin toggle (if overlay supports pinning)
- Collapse mode buttons (Expand/Minimize/Collapse)
- Auto-updates button states to reflect current mode
- Works with any DockableOverlay

**Usage:**
```java
// In your plugin panel
public void addOverlayControl(DockableOverlay overlay)
{
    OverlayControlPanel controlPanel = new OverlayControlPanel(overlay);
    overlayControlsContainer.add(controlPanel);
}
```

**Benefits:**
- No manual UI wiring needed
- Automatically detects overlay capabilities (pinnable, collapsible)
- Consistent control UI across all overlays
- Real-time state synchronization

## Interactive Overlays

### InteractiveOverlay
Base class for overlays that can handle mouse clicks and interactions.

**Features:**
- Click detection within overlay bounds
- Prevents click-through to game when handled
- Hover event support
- Right-click handling

**Usage:**
```java
public class MyInteractiveOverlay extends InteractiveOverlay
{
    @Override
    protected boolean onClicked(Point point)
    {
        // Handle left click
        if (isButtonClicked(point))
        {
            doAction();
            return true; // Consume click
        }
        return false; // Allow click-through
    }

    @Override
    protected void onHover(Point point)
    {
        // Show tooltips, highlight buttons, etc.
    }
}
```

**Click Handling:**
- Return `true` from `onClicked()` to consume the click
- Return `false` to allow click-through to game
- Use `containsPoint(point)` to check if point is in bounds

## Hotkey Support

### HotkeyListener Integration
Use RuneLite's hotkey system to add keyboard controls to overlays.

**Example:**
```java
private HotkeyListener toggleHotkey;

@Override
protected void startUp()
{
    toggleHotkey = new HotkeyListener(() -> config.toggleHotkey())
    {
        @Override
        public void hotkeyPressed()
        {
            overlay.toggleCollapse();
        }
    };

    keyManager.registerKeyListener(toggleHotkey);
}

@Override
protected void shutDown()
{
    if (toggleHotkey != null)
    {
        keyManager.unregisterKeyListener(toggleHotkey);
    }
}
```

**Common Hotkey Actions:**
- Toggle overlay visibility
- Cycle collapse modes
- Pin/unpin overlay
- Reset overlay position
- Trigger overlay-specific actions

## Graph Overlay (TODO)

### GraphOverlay Placeholder
Foundation for time-series data visualization and analytics.

**Planned Features:**
- Line graphs, bar charts, area charts
- Multiple data series support
- Axes with labels and gridlines
- Interactive tooltips on hover
- Zoom and pan controls
- Data point highlighting
- Legend/key display

**Dimensions:**
- Minimum: 300x200
- Recommended: 400x250 to 600x400
- Fully resizable like XP tracker overlays

**Current Implementation:**
- Shows placeholder with TODO list
- Demonstrates resizable overlay
- Displays current dimensions
- Ready for graph rendering implementation

## Reference Files

See `./references/` for:
- Component examples
- Layout patterns
- Color schemes
- Icon resources

See `./examples/` for:
- Complete component implementations
- Integration examples
- Common use cases
