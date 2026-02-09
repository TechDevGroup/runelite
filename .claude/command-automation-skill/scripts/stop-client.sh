#!/bin/bash
# Stop RuneLite client

echo "Stopping RuneLite client..."

# Kill all java.exe processes (Windows)
taskkill /F /IM java.exe 2>/dev/null

if [ $? -eq 0 ]; then
    echo "RuneLite client stopped"
else
    echo "No RuneLite client was running"
fi

# Wait a moment for cleanup
sleep 1
