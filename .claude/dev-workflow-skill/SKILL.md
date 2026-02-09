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

## Reference Files

See `./scripts/` for:
- watch-reload.sh - Auto-restart on build
- quick-build.sh - Fast build script
- dev-start.sh - Start with dev settings

See `./examples/` for:
- gradle.properties optimization
- Build configuration examples
