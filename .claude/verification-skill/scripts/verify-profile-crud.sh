#!/bin/bash
# Profile CRUD Verification Script
# Tests create, read, update, delete via HTTP API + browser CLI eval
# Usage: bash .claude/verification-skill/scripts/verify-profile-crud.sh

set -e

DEV_SERVER="http://localhost:3000"
DEV_SERVER_DIR="dev-server"
PASS=0
FAIL=0

json_get() {
  node -e "let s='';process.stdin.setEncoding('utf8');process.stdin.on('data',c=>s+=c);process.stdin.on('end',()=>{const d=JSON.parse(s);console.log($1)})"
}

log_pass() {
  echo "  [PASS] $1"
  PASS=$((PASS + 1))
}

log_fail() {
  echo "  [FAIL] $1"
  FAIL=$((FAIL + 1))
}

echo "=== Profile CRUD Verification ==="
echo ""

# Step 1: Health check
echo "[1/7] Server health check..."
HEALTH=$(curl -s "$DEV_SERVER/health" 2>/dev/null || echo '{"status":"error"}')
if echo "$HEALTH" | grep -q '"ok"'; then
  log_pass "Server is healthy: $HEALTH"
else
  echo "  [ABORT] Server not running: $HEALTH"
  echo "  Start with: cd dev-server && node src/server.js"
  exit 1
fi

# Step 2: Get initial profile count
echo "[2/7] Getting initial profile count..."
INITIAL_COUNT=$(curl -s "$DEV_SERVER/profiles" | json_get "d.length")
log_pass "Initial profile count: $INITIAL_COUNT"

# Step 3: Create a test profile
echo "[3/7] Creating test profile..."
CREATE_RESPONSE=$(curl -s -X POST "$DEV_SERVER/profiles" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "__verify_test_profile",
    "containerType": "INVENTORY",
    "enabled": true,
    "snapshot": {
      "containerType": "INVENTORY",
      "slotStates": {
        "0": {"itemId": 995, "itemName": "Coins", "quantity": 100, "quantityCondition": "AT_LEAST"},
        "-1": {"itemId": 4151, "itemName": "Abyssal whip", "quantity": 1, "quantityCondition": "ANY"}
      }
    }
  }')

PROFILE_ID=$(echo "$CREATE_RESPONSE" | json_get "d.id || ''")
if [ -n "$PROFILE_ID" ] && [ "$PROFILE_ID" != "undefined" ]; then
  log_pass "Created profile with ID: $PROFILE_ID"
else
  log_fail "Failed to create profile: $CREATE_RESPONSE"
fi

# Step 4: Verify profile exists in list
echo "[4/7] Verifying profile appears in list..."
AFTER_COUNT=$(curl -s "$DEV_SERVER/profiles" | json_get "d.length")
FOUND=$(curl -s "$DEV_SERVER/profiles" | json_get "d.some(p=>p.name==='__verify_test_profile')")
if [ "$FOUND" = "true" ]; then
  log_pass "Profile found in list (count: $INITIAL_COUNT -> $AFTER_COUNT)"
else
  log_fail "Profile NOT found in list after creation"
fi

# Step 5: Update the profile
echo "[5/7] Updating test profile..."
UPDATE_RESPONSE=$(curl -s -X POST "$DEV_SERVER/profiles" \
  -H "Content-Type: application/json" \
  -d "{
    \"id\": \"$PROFILE_ID\",
    \"name\": \"__verify_test_updated\",
    \"containerType\": \"INVENTORY\",
    \"enabled\": false,
    \"snapshot\": {
      \"containerType\": \"INVENTORY\",
      \"slotStates\": {
        \"0\": {\"itemId\": 995, \"itemName\": \"Coins\", \"quantity\": 200, \"quantityCondition\": \"EXACT\"}
      }
    }
  }")

UPDATED_NAME=$(echo "$UPDATE_RESPONSE" | json_get "d.name || ''")
if [ "$UPDATED_NAME" = "__verify_test_updated" ]; then
  log_pass "Profile updated: name=$UPDATED_NAME"
else
  log_fail "Failed to update profile: $UPDATE_RESPONSE"
fi

# Step 6: Delete the profile
echo "[6/7] Deleting test profile..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$DEV_SERVER/profiles/$PROFILE_ID")
if [ "$HTTP_CODE" = "200" ]; then
  log_pass "Delete returned HTTP 200"
else
  log_fail "Delete returned HTTP $HTTP_CODE (expected 200)"
fi

# Step 7: Verify profile is gone
echo "[7/7] Verifying profile deleted..."
FINAL_COUNT=$(curl -s "$DEV_SERVER/profiles" | json_get "d.length")
STILL_EXISTS=$(curl -s "$DEV_SERVER/profiles" | json_get "d.some(p=>p.id==='$PROFILE_ID')")
if [ "$STILL_EXISTS" = "false" ]; then
  log_pass "Profile removed from list (count: $AFTER_COUNT -> $FINAL_COUNT)"
else
  log_fail "Profile still exists after deletion!"
fi

# Step 8: Browser state check via CLI eval
echo ""
echo "[Browser] Checking browser state via CLI eval..."
cd "$DEV_SERVER_DIR" 2>/dev/null && {
  BROWSER_STATE=$(npm run --silent cli -- e "JSON.stringify({profileCount: document.getElementById('profile-list')?.children.length, wsState: window.__devClient?.ws?.readyState, session: window.__devClientSessionId})" 2>/dev/null | grep -o '{.*}' | tail -1)
  if [ -n "$BROWSER_STATE" ]; then
    log_pass "Browser state: $BROWSER_STATE"
  else
    log_fail "Could not read browser state (Chrome tab open?)"
  fi
  cd ..
} || {
  log_fail "Could not cd to dev-server directory"
}

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="
if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
