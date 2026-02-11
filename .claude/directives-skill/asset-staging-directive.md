# Asset Staging Directive

## Purpose
Automatically stage binary assets (icons, images, etc.) with idempotent caching, checksums, and invalidation on updates.

## Core Principles

### 1. Automatic Staging
- Assets are **automatically staged** on first access
- No manual upload/sync required for runtime
- Source of truth determines what needs staging

### 2. Idempotent Caching
- Use HTTP 304 Not Modified with ETag
- ETag = checksum (MD5/SHA256 of asset bytes)
- Client sends `If-None-Match: <etag>`
- Server responds with 304 if checksum matches

### 3. Cache Invalidation
- When asset payload changes, checksum changes
- New checksum = new ETag = cache miss
- Client automatically fetches updated asset
- Old cached versions expire naturally

## Implementation Pattern

### Database Schema
```sql
CREATE TABLE IF NOT EXISTS assets (
    id TEXT PRIMARY KEY,           -- Asset identifier (e.g., itemId)
    checksum TEXT NOT NULL,        -- MD5/SHA256 hash
    blob BLOB NOT NULL,            -- Binary data
    mime_type TEXT NOT NULL,       -- image/png, etc.
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE INDEX idx_assets_checksum ON assets(checksum);
```

### Server Endpoint Pattern
```javascript
app.get('/assets/:id', async (req, res) => {
    const asset = await store.getAsset(req.params.id);

    if (!asset) {
        return res.status(404).json({ error: 'Asset not found' });
    }

    // Check If-None-Match header
    const clientETag = req.headers['if-none-match'];
    if (clientETag === asset.checksum) {
        return res.status(304).end(); // Not Modified
    }

    // Set caching headers
    res.set({
        'ETag': asset.checksum,
        'Content-Type': asset.mime_type,
        'Cache-Control': 'public, max-age=31536000', // 1 year
    });

    res.send(asset.blob);
});
```

### Client Pattern
```javascript
async function loadAsset(id) {
    const cached = assetCache.get(id);

    const headers = {};
    if (cached?.checksum) {
        headers['If-None-Match'] = cached.checksum;
    }

    const response = await fetch(`/assets/${id}`, { headers });

    if (response.status === 304) {
        return cached.data; // Use cached version
    }

    const blob = await response.blob();
    const checksum = response.headers.get('etag');

    assetCache.set(id, { data: blob, checksum });
    return blob;
}
```

### Automatic Staging Pattern
```javascript
// On first access, stage the asset if missing
async function ensureAssetStaged(assetId, fetchFn) {
    const exists = await store.assetExists(assetId);

    if (!exists) {
        const blob = await fetchFn(assetId); // Fetch from source
        const checksum = computeChecksum(blob);

        await store.saveAsset({
            id: assetId,
            blob,
            checksum,
            mime_type: 'image/png',
            created_at: Date.now(),
            updated_at: Date.now()
        });

        log.info(`[AssetStaging] Staged asset ${assetId} (checksum: ${checksum})`);
    }

    return store.getAsset(assetId);
}
```

## Benefits

1. **Zero Manual Sync** - Assets appear automatically on first use
2. **Efficient Bandwidth** - 304 responses are tiny (no body)
3. **Automatic Updates** - Changed assets get new checksums
4. **Long Cache TTL** - Clients cache for 1 year safely
5. **Idempotent** - Multiple requests for same asset = same result

## Example: Icon System

### Current Flow (Manual Sync)
1. User clicks "Sync Icons" button
2. Plugin extracts all icons
3. Sends icons to server via WebSocket
4. Server stores in database
5. Browser can now fetch icons

### New Flow (Automatic Staging)
1. Game state streams item IDs: [995, 2566, 8007]
2. Browser tries to fetch `/icons/995`
3. Server checks: icon 995 missing
4. Server requests from plugin via WebSocket
5. Plugin sends icon 995
6. Server stores + returns to browser
7. Next request: 304 Not Modified (cached)

### Implementation
```javascript
// Server: Icon endpoint with auto-staging
app.get('/icons/:itemId', async (req, res) => {
    const itemId = parseInt(req.params.itemId);

    // Try to get from database
    let icon = await store.getIcon(itemId);

    // If missing, request from plugin
    if (!icon) {
        icon = await requestIconFromPlugin(itemId);
        if (!icon) {
            return res.status(404).json({ error: 'Icon not available' });
        }
    }

    // Standard 304 caching logic
    const clientETag = req.headers['if-none-match'];
    if (clientETag === icon.checksum) {
        return res.status(304).end();
    }

    res.set({
        'ETag': icon.checksum,
        'Content-Type': 'image/png',
        'Cache-Control': 'public, max-age=31536000'
    });

    res.send(icon.blob);
});
```

## Testing Checklist

- [ ] First request: 200 OK with ETag header
- [ ] Second request with If-None-Match: 304 Not Modified
- [ ] Asset update: New checksum, cache miss, 200 OK
- [ ] Missing asset: 404 Not Found
- [ ] Concurrent requests: Only one staging operation
- [ ] Network error: Graceful fallback

## References

- RFC 7232: HTTP/1.1 Conditional Requests
- RFC 9110: HTTP Semantics (ETag, Cache-Control)
- MDN: HTTP Caching
