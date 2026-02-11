#!/bin/bash
# Quick rebuild and restart cycle

set -e

JAVA_HOME="$(pwd)/.jdk/jdk-11.0.30+7"
BUILD_DIR="runelite/runelite-client/build/libs"

echo "Building RuneLite client..."
export JAVA_HOME
cd runelite && ./gradlew :client:shadowJar -x test --build-cache -Dorg.gradle.dependency.verification=off

if [ $? -eq 0 ]; then
    echo "Build successful, restarting client..."
    cd ..

    # Kill old client
    taskkill //F //IM java.exe 2>/dev/null || true

    sleep 2

    # Start new client using launcher script
    cmd.exe /c "scripts\launch-runelite.cmd"

    echo "Client restarted"
else
    echo "Build failed, not restarting"
    exit 1
fi
