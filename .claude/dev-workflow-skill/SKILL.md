---
name: dev-workflow
description: Fast development workflow for RuneLite plugins including hot-reload strategies, continuous build, and rapid iteration techniques. Use this skill to set up efficient development environments and minimize build/test cycles.
license: MIT
---

# Development Workflow Skill

## Purpose
Provides strategies and tools for rapid plugin development with minimal build/restart cycles.

## Development Modes

### 1. Debug Mode
Launch with debug logging enabled:

```bash
java -jar runelite-client/build/libs/client-*-shaded.jar --debug
```

Benefits:
- Verbose logging to console
- Debug-level log messages visible
- Useful for troubleshooting

### 2. Continuous Build Mode
Gradle can watch for changes and automatically rebuild:

```bash
./gradlew :client:build --continuous
```

This monitors source files and rebuilds when changes are detected.

### 3. Incremental Compilation
Use Gradle's build cache and incremental compilation:

```bash
./gradlew :client:build --build-cache
```

Speeds up subsequent builds by only recompiling changed files.

## Hot Reload Strategies

### Option 1: DCEVM + HotswapAgent (Recommended for Development)
Free, open-source solution for true hot-reloading.

**Setup:**
1. Download DCEVM (Dynamic Code Evolution VM)
2. Download HotswapAgent
3. Run with:
```bash
java -XXaltjvm=dcevm -javaagent:hotswap-agent.jar -jar client.jar
```

**Capabilities:**
- Reload method bodies
- Add/remove methods
- Modify class structure
- No restart needed for most changes

### Option 2: JRebel (Commercial)
Commercial hot-reload solution with advanced features.

**Setup:**
1. Install JRebel
2. Configure for Gradle project
3. Run with JRebel agent

**Capabilities:**
- Hot reload almost any code change
- Framework-aware reloading
- Very fast iteration

### Option 3: Spring Boot DevTools Pattern
Implement a custom classloader-based reload mechanism.

**Approach:**
- Separate plugin classloader
- Watch for .class file changes
- Reload plugin classes dynamically

### Option 4: Fast Build + Auto-Restart
Script-based approach for quick iteration without true hot-reload.

**Workflow:**
1. Gradle continuous build in background
2. File watcher detects new JAR
3. Auto-restart client with new JAR

See `./scripts/watch-and-reload.sh` for implementation.

## Fast Iteration Without Hot Reload

### Optimize Gradle Build

**1. Parallel Execution**
```bash
./gradlew build --parallel --max-workers=4
```

**2. Daemon Mode**
```bash
./gradlew build --daemon
```

**3. Configuration Cache**
```bash
./gradlew build --configuration-cache
```

**4. Skip Unnecessary Tasks**
```bash
# Skip tests during development
./gradlew :client:build -x test

# Skip javadoc generation
./gradlew :client:build -x javadoc
```

### Build Only What Changed

```bash
# Build specific module
./gradlew :client:classes

# Skip signing (development only)
./gradlew :client:jar -x jarSign
```

## Development Gradle Tasks

### Quick Build (No Tests)
```bash
./gradlew :client:assemble
```
Faster than full build, skips tests and checks.

### Incremental Compile
```bash
./gradlew :client:compileJava
```
Just compile Java files, no packaging.

### Build and Run
```bash
./gradlew :client:shadowJar && java -jar runelite-client/build/libs/client-*-shaded.jar
```

## Watch and Reload Script

Create `.claude/dev-workflow-skill/scripts/watch-reload.sh`:

```bash
#!/bin/bash

JAVA_HOME=".jdk/jdk-11.0.30+7"
BUILD_DIR="runelite/runelite-client/build/libs"
JAR_PATTERN="client-*-shaded.jar"

echo "Starting continuous build..."
cd runelite
./gradlew :client:shadowJar --continuous --build-cache -x test &
BUILD_PID=$!

sleep 10  # Wait for initial build

echo "Starting watch and reload loop..."
cd ..

prev_mtime=0
while true; do
    jar_file=$(ls -t $BUILD_DIR/$JAR_PATTERN 2>/dev/null | head -1)

    if [ -f "$jar_file" ]; then
        curr_mtime=$(stat -c %Y "$jar_file" 2>/dev/null || stat -f %m "$jar_file")

        if [ "$curr_mtime" != "$prev_mtime" ] && [ "$prev_mtime" != "0" ]; then
            echo "Detected new build, restarting client..."

            # Kill previous client if running
            pkill -f "client-.*-shaded.jar"

            # Start new client
            $JAVA_HOME/bin/java -jar "$jar_file" --debug &

            echo "Client restarted with updated code"
        fi

        prev_mtime=$curr_mtime
    fi

    sleep 2
done
```

## In-App Development Console

For real-time logging without console window:

1. Enable dev console in plugin
2. Use `DevConsoleComponent` to display logs in-app
3. Configure in plugin config:
   - Console font size
   - Max lines
   - Show timestamps
   - Auto-scroll

**Example:**
```java
@ConfigItem(keyName = "devMode", name = "Dev Mode")
default boolean devMode() { return false; }

// In plugin
if (config.devMode())
{
    panel.logDebug("Development message");
}
```

## Recommended Workflow

### Setup Phase (Once)
1. Configure Gradle build cache
2. Enable parallel execution in gradle.properties
3. Set up DCEVM if using hot-reload
4. Create watch-reload script

### Development Phase (Daily)
1. Start continuous build in one terminal:
   ```bash
   ./gradlew :client:shadowJar --continuous -x test --build-cache
   ```

2. Launch client in debug mode:
   ```bash
   java -jar runelite-client/build/libs/client-*-shaded.jar --debug
   ```

3. Make code changes

4. For methods/logic changes:
   - If using DCEVM: Changes apply immediately
   - Otherwise: Restart client (builds automatically)

5. For structural changes (new classes, fields):
   - Always requires restart
   - Wait for continuous build to finish
   - Restart client

### Quick Test Cycle
```bash
# Terminal 1: Continuous build
./gradlew :client:classes --continuous

# Terminal 2: Quick restart script
while true; do
    java -jar runelite-client/build/libs/client-*-shaded.jar
    sleep 1
done
```

Press Ctrl+C in Terminal 2 to restart client after code changes.

## Gradle Properties Optimization

Add to `gradle.properties`:

```properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
```

## Build Performance Tips

1. **Use SSD** for project directory (major impact)
2. **Increase Gradle memory**: `-Xmx2g` or higher
3. **Skip unnecessary plugins**: Comment out unused plugins during development
4. **Local Maven cache**: Ensure dependencies are cached locally
5. **Disable virus scanner** for project directory (if safe)

## Troubleshooting Slow Builds

### Check Build Scan
```bash
./gradlew :client:build --scan
```
Provides detailed build performance analysis.

### Profile Build
```bash
./gradlew :client:build --profile
```
Generates HTML report showing task duration.

### Clear Caches
If builds seem stuck:
```bash
./gradlew clean cleanBuildCache
rm -rf ~/.gradle/caches
```

## Remote Debugging

For step-through debugging with IDE:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar client-*-shaded.jar
```

Then connect IDE debugger to localhost:5005.

## Best Practices

1. **Use dev console** for in-app logging during testing
2. **Enable debug mode** for verbose logs
3. **Continuous build** for automatic recompilation
4. **Skip tests** during rapid iteration (run before commits)
5. **Profile builds** if slow
6. **Use DCEVM** for true hot-reload when possible
7. **Create restart scripts** for quick client restart
8. **Monitor build output** for errors

## IDE Integration

### IntelliJ IDEA
- Enable "Build project automatically"
- Use "Run with Coverage" for development
- Configure hot-swap settings
- Use "Recompile" (Ctrl+Shift+F9) for quick rebuild

### VS Code
- Use Gradle extension
- Configure tasks.json for quick builds
- Terminal for continuous build

## Advanced: Hot-Reload Dev Server System

### Overview
Complete zero-rebuild development system using browser-based UI, WebSocket communication, and Chrome DevTools Protocol automation.

**Architecture:**
```
CLI Tool (Node.js)
    ↓ Chrome DevTools Protocol
Chrome Browser (localhost:3001)
    ↓ WebSocket
Dev Server (localhost:3000)
    ↓ WebSocket
RuneLite Plugin (Java)
```

**Benefits:**
- Zero rebuilds for plugin behavior changes
- Browser-based profile management UI
- Programmatic automation via CLI
- Digital twin preview (browser renders what plugin renders)
- Persistent state in SQLite
- Hot Module Replacement (HMR) for instant UI updates
- Agent-driven development capabilities

### Components

#### 1. Dev Server (Port 3000)
HTTP/WebSocket server serving as source of truth for all artifacts.

**Location:** `dev-server/`

**Files:**
- `src/server.js` - Express + WebSocket server
- `src/broker.js` - Event routing with O(1) lookups
- `src/store.js` - SQLite persistence layer
- `vite.config.js` - Vite configuration for browser client

**Features:**
- REST API for profiles, icons, game state
- WebSocket pub/sub for real-time updates
- Artifact registry (tracks browser vs plugin origin)
- SQLite persistence

#### 2. Browser Client (Port 3001)
Vite-powered browser UI for managing plugin behavior.

**Location:** `dev-server/client/`

**Files:**
- `index.html` - Main UI
- `src/client.js` - WebSocket client with auto-reconnect
- `src/digital-twin.js` - Canvas renderer for preview
- `src/main.js` - App logic + global exposure for CLI

**Features:**
- Profile CRUD operations
- Digital twin preview (renders inventory overlays)
- Real-time game state display
- Hot Module Replacement (instant updates)

#### 3. CLI Tool
Chrome DevTools Protocol automation for programmatic control.

**Location:** `dev-server/src/`

**Files:**
- `cli.js` - Interactive REPL + single-command mode
- `browser-launcher.js` - Launches Chrome with CDP
- `browser-controller.js` - Executes JavaScript in browser context

**Features:**
- Launch Chrome with debugging enabled
- Execute commands in browser context
- Create profiles programmatically
- Sync icons from plugin
- View game state
- Agent-driven automation

#### 4. Java Plugin Integration
WebSocket client for connecting RuneLite to dev server.

**Location:** `runelite/runelite-client/src/main/java/net/runelite/client/plugins/runeutils/`

**Files:**
- `DevServerClient.java` - WebSocket client
- `HotReloadManager.java` - Profile hot-reload
- `GameStateStreamer.java` - Streams game state at 500ms
- `IconExtractor.java` - Extracts item icons

**Features:**
- Real-time game state streaming
- Hot-reload profiles without restart
- Icon extraction and upload
- Event-driven architecture

### Setup Workflow

#### Step 1: Install Dependencies
```bash
cd dev-server
npm install
```

#### Step 2: Start Dev Server (Terminal 1)
```bash
cd dev-server
npm run dev
```

**Expected Output:**
```
╔════════════════════════════════════════╗
║   RuneUtils Dev Server                 ║
║   HTTP: http://localhost:3000       ║
║   WebSocket: ws://localhost:3000/ws ║
╚════════════════════════════════════════╝
```

#### Step 3: Start Vite Client (Terminal 2)
```bash
cd dev-server
npm run dev:client
```

**Expected Output:**
```
VITE ready in 449 ms
➜  Local:   http://localhost:3001/
```

**Alternatively, start both concurrently:**
```bash
cd dev-server
npm run dev:all
```

#### Step 4: Build and Launch RuneLite with Dev Mode
```bash
cd runelite
./gradlew :client:shadowJar -x test
java -Druneutils.devserver=ws://localhost:3000/ws \
     -jar runelite-client/build/libs/client-*-shaded.jar
```

**Plugin will log:**
```
[DevServer] Connected to ws://localhost:3000/ws
[HotReload] Manager started
```

#### Step 5: Open Browser or Use CLI
**Option A: Manual (Browser)**
Open `http://localhost:3001` in your browser.

**Option B: Automated (CLI)**
```bash
cd dev-server
npm run cli
```

CLI will:
1. Launch Chrome to `http://localhost:3001`
2. Connect via Chrome DevTools Protocol
3. Establish WebSocket to dev server
4. Enter interactive REPL mode

### Daily Development Workflow

#### Terminal Setup
```bash
# Terminal 1: Dev server (with auto-restart)
cd dev-server && npm run dev

# Terminal 2: Vite client (with HMR)
cd dev-server && npm run dev:client

# Terminal 3: RuneLite (rebuild only when exposing new capabilities)
cd runelite
java -Druneutils.devserver=ws://localhost:3000/ws \
     -jar runelite-client/build/libs/client-*-shaded.jar
```

#### Making Changes

**1. Profile Changes (Zero Rebuild)**
- Modify profiles in browser UI
- Changes persist to SQLite automatically
- Plugin hot-reloads profiles via WebSocket
- No restart needed

**2. UI Changes (HMR)**
- Edit `dev-server/client/src/*.js`
- Vite HMR applies instantly
- No manual reload needed

**3. Server Logic Changes (Nodemon)**
- Edit `dev-server/src/server.js`
- Nodemon auto-restarts server
- Reconnects automatically

**4. Plugin Code Changes (Rebuild Required)**
Only needed when:
- Exposing new game data
- Adding new WebSocket event handlers
- Changing core plugin logic

```bash
cd runelite
./gradlew :client:shadowJar -x test
# Restart RuneLite
```

### CLI Commands

#### Interactive Mode
```bash
npm run cli
```

**Commands:**
```
profile:create <name> [type]  - Create new profile
profile:list                  - List all profiles
icons:sync                    - Sync icons from plugin
browser:reload                - Reload browser tab
game:state                    - Show current game state
status                        - Show connection status
help                          - Show help
exit                          - Exit CLI
```

#### Single-Command Mode
```bash
npm run cli profile:create "Slayer Setup" INVENTORY
npm run cli profile:list
npm run cli icons:sync
npm run cli status
```

### Programmatic Usage (Agent-Driven)

#### JavaScript Automation
```javascript
import { BrowserController } from './src/browser-controller.js';
import { BrowserLauncher } from './src/browser-launcher.js';

async function automateProfiles() {
  const launcher = new BrowserLauncher();
  const connInfo = await launcher.launch();

  const tab = await launcher.findTabByUrl('localhost:3001');
  const controller = new BrowserController(tab.webSocketDebuggerUrl);
  await controller.connect();

  // Create multiple profiles
  const configs = [
    { name: 'Slayer Setup', type: 'INVENTORY' },
    { name: 'PvM Setup', type: 'INVENTORY' },
    { name: 'Skilling Setup', type: 'INVENTORY' }
  ];

  for (const config of configs) {
    await controller.createProfile(config.name, config.type);
    console.log(`✓ Created: ${config.name}`);
  }
}
```

#### Spawn CLI from Scripts
```javascript
import { spawn } from 'child_process';

function runCLICommand(command, args = []) {
  return new Promise((resolve, reject) => {
    const proc = spawn('npm', ['run', 'cli', command, ...args]);

    let output = '';
    proc.stdout.on('data', (data) => {
      output += data.toString();
    });

    proc.on('close', (code) => {
      if (code === 0) resolve(output);
      else reject(new Error(`Command failed with code ${code}`));
    });
  });
}

await runCLICommand('profile:create', ['Bot Profile', 'INVENTORY']);
```

### Chrome DevTools Protocol Details

#### Connection Flow
1. Launch Chrome with `--remote-debugging-port=9222`
2. Query `http://localhost:9222/json/list` for targets
3. Find dev console tab by URL pattern
4. Connect to `ws://localhost:9222/devtools/page/<page-id>`
5. Send CDP commands via WebSocket

#### Key CDP Commands
- `Runtime.evaluate` - Execute JavaScript in page context
- `Page.reload` - Reload the page
- `Page.navigate` - Navigate to URL
- `Console.enable` - Enable console logging

#### Browser API Exposure
The client exposes globals for CLI access:
```javascript
window.__devClient         // DevClient instance
window.__devClientSessionId // WebSocket session ID
window.__gameState          // Latest game state from plugin
```

### Data Flow Examples

#### Profile Creation (Browser → Plugin)
1. User creates profile in browser UI
2. Browser sends POST to `/api/profiles`
3. Dev server saves to SQLite
4. Dev server broadcasts `profile_update` via WebSocket
5. Plugin receives event, hot-reloads profile
6. Plugin UI updates without restart

#### Game State Streaming (Plugin → Browser)
1. Plugin detects inventory change
2. `GameStateStreamer` captures state every 500ms
3. Sends `game_state` message via WebSocket to dev server
4. Dev server broadcasts to all connected browser clients
5. Browser updates digital twin preview

#### Icon Sync (Plugin → Browser)
1. User clicks "Sync Icons" in browser or CLI
2. Browser sends command via WebSocket
3. Dev server forwards to plugin
4. `IconExtractor` extracts 30k item icons
5. Uploads as base64 to dev server
6. Dev server stores in SQLite
7. Browser caches icons in Map for O(1) lookup

### Performance Notes

**Latency:**
- CDP commands: ~10-50ms
- JavaScript eval: ~5-20ms
- Profile creation: ~100ms end-to-end
- Icon sync: ~30 seconds for all items
- Game state stream: 500ms interval (throttled)

**Optimization Patterns:**
- O(1) lookups via Map pre-curation
- WebSocket multiplexing (single connection)
- Icon caching (browser + SQLite)
- Throttled game state updates
- Event-driven (no polling loops)

### Troubleshooting

#### Chrome won't launch
```bash
# Check Chrome path
which chrome  # Linux/Mac
where chrome  # Windows

# Try manual launch
chrome --remote-debugging-port=9222 http://localhost:3001
```

#### Port already in use
```bash
# Find process on port 3000
netstat -ano | findstr :3000  # Windows
lsof -i :3000                 # Linux/Mac

# Kill process
taskkill //F //PID <pid>      # Windows
kill -9 <pid>                 # Linux/Mac
```

#### WebSocket connection fails
- Check dev server is running on port 3000
- Verify browser is on `localhost:3001`
- Check browser console for errors
- Verify `window.__devClient` exists

#### Plugin not connecting
- Check `-Druneutils.devserver=ws://localhost:3000/ws` flag
- Verify dev server WebSocket endpoint
- Check plugin logs for connection errors
- Ensure WebSocket library in `build.gradle.kts`

#### CLI commands timeout
- Check Chrome is still open
- Verify browser tab hasn't crashed
- Check CDP port 9222 is accessible
- Restart CLI tool

### Security Considerations

**⚠️ Development Mode Only**

- Never expose CDP port (9222) to network
- Keep `--remote-debugging-port` local only
- Don't run dev server in production
- CDP gives full browser control (execute any JS)
- SQLite database is unencrypted

### File Structure Reference

```
dev-server/
├── src/
│   ├── server.js              # Main HTTP/WS server
│   ├── broker.js              # Event routing
│   ├── store.js               # SQLite persistence
│   ├── cli.js                 # CLI tool
│   ├── browser-launcher.js    # Chrome launcher
│   └── browser-controller.js  # CDP controller
├── client/
│   ├── index.html             # Browser UI
│   └── src/
│       ├── client.js          # WebSocket client
│       ├── digital-twin.js    # Canvas renderer
│       └── main.js            # App logic
├── package.json               # Dependencies
├── vite.config.js             # Vite config
├── ARCHITECTURE.md            # Architecture docs
├── README.md                  # Setup guide
└── CLI-GUIDE.md               # CLI documentation

runelite/runelite-client/src/main/java/.../runeutils/
├── DevServerClient.java       # WebSocket client
├── HotReloadManager.java      # Profile hot-reload
├── GameStateStreamer.java     # Game state streaming
├── IconExtractor.java         # Icon extraction
└── RuneUtilsPlugin.java       # Plugin integration
```

### Future Enhancements

- [ ] Profile templates (load from YAML)
- [ ] Batch operations (bulk create/delete)
- [ ] Performance profiling tools
- [ ] Multi-browser support
- [ ] Remote CLI (SSH tunneling)
- [ ] WebRTC for direct plugin ↔ browser communication
- [ ] GraphQL API for complex queries
- [ ] Real-time collaboration (multiple devs)

### Summary

This hot-reload system enables **zero-touch plugin development**:

```
Old: Edit code → Rebuild → Restart → Test
New: Edit profile in browser → Instant update → Test
```

Perfect for agent-driven development where LLMs can directly control the plugin system through CLI automation!

## Reference Files

See `./scripts/` for:
- watch-reload.sh - Auto-restart on build
- quick-build.sh - Fast build script
- dev-start.sh - Start with dev settings

See `./examples/` for:
- gradle.properties optimization
- Build configuration examples
