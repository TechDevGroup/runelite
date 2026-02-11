---
name: verification-skill
description: End-to-end verification workflows using CLI tools, HTTP APIs, and browser page instance evaluation. Used to validate state consistency across the dev server, browser frontend, and RuneLite plugin after code changes.
license: MIT
---

# Verification Skill

## Purpose
Provides structured verification workflows to validate system state after code changes. Uses:
- **HTTP API** (`curl`) for server/DB state
- **CLI eval** (`npm run cli e "..."`) for browser page instance state
- **Dev server health** for connection status

## When to Use
- After every implementation task (alternating: implement, then verify)
- After rebuilds to confirm no regressions
- Before committing to ensure system is stable

## Verification Layers

### Layer 1: Server State (HTTP API)
Direct database/server queries via HTTP endpoints.

```bash
# Health check - server status + connected clients
curl -s http://localhost:3000/health

# All profiles from DB
curl -s http://localhost:3000/profiles

# Profile checksum (state hash for sync comparison)
curl -s http://localhost:3000/profiles/checksum

# All icons metadata
curl -s http://localhost:3000/icons

# Specific icon (returns PNG with ETag caching)
curl -sI http://localhost:3000/icons/995
```

### Layer 2: Browser State (CLI Page Instance Eval)
Execute JavaScript in the browser context via Chrome DevTools Protocol.

```bash
# Connection status
npm run cli e "JSON.stringify({ws: window.__devClient?.ws?.readyState, session: window.__devClientSessionId})"

# Profiles via browser fetch (tests Vite proxy + API)
npm run cli e "(async()=>{const r=await fetch('/api/profiles');return await r.json()})()"

# DOM profile count
npm run cli e "document.getElementById('profile-list')?.children.length"

# Browser event log (last 5 entries)
npm run cli e "Array.from(document.getElementById('event-log')?.children||[]).slice(-5).map(e=>e.textContent)"

# Icon fetch test via browser proxy
npm run cli e "(async()=>{const r=await fetch('/api/icons/995');return{status:r.status,type:r.headers.get('content-type')}})()"
```

### Layer 3: Full E2E Verification
Single eval that checks all layers at once.

```bash
npm run cli e "(async()=>{
  const health=await(await fetch('/api/health')).json();
  const profiles=await(await fetch('/api/profiles')).json();
  const checksum=await(await fetch('/api/profiles/checksum')).json();
  const icons=await(await fetch('/api/icons')).json();
  const dom=document.getElementById('profile-list');
  return{
    server:{status:health.status,clients:health.clients},
    profiles:{count:profiles.length,names:profiles.map(p=>p.name)},
    checksum:checksum.checksum,
    icons:{count:icons.length},
    browser:{ws:window.__devClient?.ws?.readyState,dom:dom?.children.length}
  }
})()"
```

## Verification Workflows

### Workflow 1: After Profile Code Changes
1. Rebuild client: `bash .claude/command-automation-skill/scripts/rebuild.sh`
2. Check server profiles: `curl -s http://localhost:3000/profiles`
3. Check browser DOM: `npm run cli e "document.getElementById('profile-list')?.children.length"`
4. Verify no duplicates: profile count should match DB count

### Workflow 2: After Icon Changes
1. Check icon DB: `curl -s http://localhost:3000/icons`
2. Test icon HTTP endpoint: `curl -sI http://localhost:3000/icons/{itemId}`
3. Verify browser can fetch: `npm run cli e "(async()=>{const r=await fetch('/api/icons/{itemId}');return r.status})()"`
4. Confirm ETag header present for caching

### Workflow 3: After Sync/WebSocket Changes
1. Check server health: `curl -s http://localhost:3000/health` (verify client count)
2. Check browser WebSocket: `npm run cli e "window.__devClient?.ws?.readyState"` (should be 1 = OPEN)
3. Check checksum: `curl -s http://localhost:3000/profiles/checksum`
4. Run full E2E verification (Layer 3)

### Workflow 4: After Dev Server Restart
1. Kill old process: `taskkill //F //PID {pid}`
2. Start new: `cd dev-server && node src/server.js &`
3. Wait 3 seconds for startup
4. Verify health: `curl -s http://localhost:3000/health`
5. Verify clients reconnect (health shows correct client count)

## Expected Values

| Check | Expected |
|-------|----------|
| `health.status` | `"ok"` |
| `health.clients` | 2+ (browser + RuneLite) |
| `ws.readyState` | `1` (OPEN) |
| Profile count (DB vs DOM) | Must match |
| Icon endpoint Content-Type | `image/png` |
| Icon endpoint ETag | Present (MD5 hash) |
| Checksum endpoint | Non-null hash string |

## Common Issues

### Profile Duplicates
- **Symptom**: DOM count > DB count
- **Cause**: Multiple sources adding to panel independently
- **Fix**: ArtifactManager must be single source of truth

### WebSocket Not Connected
- **Symptom**: `ws.readyState !== 1`
- **Cause**: Dev server not running or port conflict
- **Check**: `netstat -ano | findstr :3000`

### Icons Return 404
- **Symptom**: `/icons/{id}` returns 404
- **Cause**: Icons not synced from RuneLite yet
- **Fix**: Run icon sync from browser ("Sync Icons from Plugin" button) while in-game

### Checksum Mismatch
- **Symptom**: Client requests full sync every time
- **Cause**: Checksum not being stored/sent properly
- **Check**: Verify `profiles_checksum` message in WebSocket traffic

## Automated Verification Scripts

### Profile CRUD Verification
Tests the full create/read/update/delete lifecycle via HTTP API, then checks browser state via CLI eval.

```bash
bash .claude/verification-skill/scripts/verify-profile-crud.sh
```

**What it does:**
1. Health check (server running?)
2. Create a test profile via POST /profiles
3. Verify it appears in GET /profiles
4. Update the profile (name + items change)
5. Delete via DELETE /profiles/:id
6. Verify it's removed from the list
7. Check browser DOM state via CLI eval

**Script location:** `.claude/verification-skill/scripts/verify-profile-crud.sh`

## Integration with Other Skills
- **command-automation-skill**: Use `/rebuild` before verification
- **lessons-learned-skill**: Document new verification failures
- **directives-skill**: Verification is mandatory after implementation tasks
