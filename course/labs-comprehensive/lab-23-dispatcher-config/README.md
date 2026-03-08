# Lab 23: Dispatcher Configuration with AI
# Comprehensive Lab - 4 hours

## Objective

Configure Apache Dispatcher for AEM with AI-assisted optimization. Use TDD to validate caching rules, security headers, and load balancing configurations.

---

## Prerequisites

- Lab 8 (Deployment) completed
- Lab 20 (AEM Cloud) helpful

---

## BMAD Phase Context

```
Phase 03 (Architecture) - Infrastructure Design
Phase 04 (Development) - Dispatcher Configuration
Phase 05 (Testing) - Validation & Performance
```

---

## Part 1: Dispatcher Configuration Spec (TDD - RED) (30 min)

### 1.1 Write Dispatcher Tests First

Create `dispatcher/src/test/dispatcher.spec.ts`:

```typescript
import { describe, it, expect } from 'vitest';
import { parseDispatcherConfig, validateRules } from './dispatcher-validator';

describe('Dispatcher Configuration Specifications', () => {
  
  describe('Caching Rules', () => {
    
    it('should cache dam assets with appropriate TTL', () => {
      const config = parseDispatcherConfig(dispatcherConf);
      
      const damRule = config.rules.find(r => r.path === '/content/dam/*');
      
      expect(damRule).toBeDefined();
      expect(damRule.ttl).toBeLessThanOrEqual(86400); // max 24 hours
      expect(damRule.allowAuthorized).toBe(true);
    });
    
    it('should not cache personalized content', () => {
      const config = parseDispatcherConfig(dispatcherConf);
      
      const personalRule = config.rules.find(r => 
        r.path.includes('personalized'));
      
      expect(personalRule?.cache).toBe('never');
    });
  });
  
  describe('Security Headers', () => {
    
    it('should include CSRF token in responses', () => {
      const headers = parseHeadersConf(headersConf);
      
      expect(headers).toContain('CSRF-Token');
    });
    
    it('should set strict CSP headers', () => {
      const headers = parseHeadersConf(headersConf);
      
      const csp = headers.find(h => h.name === 'Content-Security-Policy');
      expect(csp.value).toContain("script-src 'self'");
      expect(csp.value).not.toContain("'unsafe-inline'");
    });
  });
  
  describe('Rewrite Rules', () => {
    
    it('should redirect .html to extensionless URLs', () => {
      const rules = parseRewrites(rewriteRules);
      
      const htmlRule = rules.find(r => 
        r.match === '*.html' && r.redirect === 301);
      
      expect(htmlRule).toBeDefined();
    });
  });
  
  describe('Filter Rules', () => {
    
    it('should block admin paths from publish', () => {
      const filters = parseFilters(dispatcherConf);
      
      const adminFilter = filters.find(f => 
        f.path === '/system/admin*' && f.deny === true);
      
      expect(adminFilter).toBeDefined();
    });
  });
});
```

---

## Part 2: Dispatcher Configuration (TDD - GREEN) (45 min)

### 2.1 AI Generate Configuration

```bash
goose run --task "Generate Apache Dispatcher configuration for AEM:
1. dispatcher.conf - main config
2. available_vhosts/*.vhost - virtual hosts
3. filters.any - URL filters
4. caches.any - caching rules
5. rewrites.any - rewrite rules
6. headers.any - security headers

Include:
- Caching for /content/dam
- Security headers (CSP, HSTS)
- GraphQL endpoint proxy
- Workflow API exposure"
```

### 2.2 Main Configuration

Create `dispatcher/src/conf.d/dispatcher.any`:

```
/virtualhosts
{
    "www.example.com"
}

/filters
{
    /0001 { /type "allow" /url "/content/*" }
    /0002 { /type "allow" /url "/graphql/execute.json/*" }
    /0003 { /type "allow" "/api/*" }
    /0099 { /type "deny" "/system/*" }
}

/cache
{
    /rules
    {
        /0000 { /glob "*" /type "deny" }
        /0001 { /type "allow" "/content/dam/*" }
        /0002 { /type "allow" "*.html" }
    }
    
    /invalidate
    {
        /0000 { /glob "*" /type "deny" }
        /0001 { /type "allow" "/content/*" }
    }
}

/headers
{
    "Cache-Control"
    "Content-Disposition"
    "CSRF-Token"
}
```

### 2.3 Virtual Host

Create `dispatcher/src/conf.d/available_vhosts/demo.vhost`:

```apache
<VirtualHost *:80>
    ServerName www.example.com
    ServerAlias example.com
    
    # Logging
    CustomLog ${CONTEXT_DOCUMENT_ROOT}/logs/access.log combined
    ErrorLog ${CONTEXT_DOCUMENT_ROOT}/logs/error.log
    
    # Security Headers
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set X-Content-Type-Options "nosniff"
    Header always set Referrer-Policy "strict-origin-when-cross-origin"
    Header always set Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:;"
    
    # HSTS (only on production)
    # Header always set Strict-Transport-Security "max-age=31536000"
    
    # Rewrite rules
    RewriteEngine On
    RewriteCond %{REQUEST_URI} !\.html$
    RewriteRule ^/(.*)\.html$ /$1 [R=301,L]
    
    # GraphQL endpoint
    ProxyPass /graphql http://localhost:4502/graphql
    ProxyPassReverse /graphql http://localhost:4502/graphql
    
    # Include default dispatcher
    Include conf.dispatcher.d/*.conf
</VirtualHost>
```

---

## Part 3: Caching Strategy (30 min)

### 3.1 Cache Invalidation

```apache
# Cache invalidation for DAM
/filter
{
    /0001 { /type "allow" /url "/content/dam/*" }
}

# Invalidate on publish
/invalidate
{
    /0000 { /glob "*" /type "deny" }
    /0001 { /type "allow" "/content/dam/*" }
    /0002 { /type "allow" "/conf/*" }
}
```

### 3.2 TTL Configuration

```
/cache
{
    /statfileslevel "2"
    
    /serveStaleOnError "1"
    
    /ttl
    {
        "/content/dam/*" "86400"        # 24 hours
        "/content/experience-fragments/*" "3600"  # 1 hour
        "*.html" "0"                     # No cache for HTML
    }
}
```

---

## Part 4: Security Configuration (30 min)

### 4.1 Filter Rules

```
# Deny access to sensitive paths
/filters
{
    # Block admin
    /0001 { /type "deny" /path "/system/*" }
    /0002 { /type "deny" /path "/bin/*" }
    /0003 { /type "deny" /path "/crx/*" }
    /0004 { /type "deny" /path "/login/*" }
    
    # Allow public
    /0100 { /type "allow" /url "/content/*" }
    /0101 { /type "allow" /url "/libs/*" }
    /0102 { /type "allow" /url "/graphql/*" }
}
```

### 4.2 CORS Configuration

```apache
# CORS headers for API access
Header set Access-Control-Allow-Origin "https://app.example.com"
Header set Access-Control-Allow-Methods "GET, POST, OPTIONS"
Header set Access-Control-Allow-Headers "Authorization, Content-Type, CSRF-Token"
Header set Access-Control-Max-Age "3600"

# Handle preflight
RewriteEngine On
RewriteCond %{REQUEST_METHOD} OPTIONS
RewriteRule ^(.*)$ $1 [R=200,L]
```

---

## Part 5: GraphQL & API Proxy (30 min)

### 5.1 GraphQL Proxy

```apache
# GraphQL endpoint
<Location /graphql>
    ProxyPass http://localhost:4502/graphql
    ProxyPassReverse http://localhost:4502/graphql
    
    # Cache GraphQL responses
    CacheStaleOnError On
    CacheLock On
</Location>

# Persisted queries
<Location /graphql/execute.json>
    ProxyPass http://localhost:4502/graphql/execute.json
    ProxyPassReverse http://localhost:4502/graphql/execute.json
</Location>
```

### 5.2 API Proxy

```apache
# REST API
<Location /api/assets>
    ProxyPass http://localhost:4502/api/assets
    ProxyPassReverse http://localhost:4502/api/assets
    AllowMethods GET POST PUT DELETE
</Location>

# Workflow API
<Location /api/workflow>
    ProxyPass http://localhost:4502/api/workflow
    ProxyPassReverse http://localhost:4502/api/workflow
    Require user admin
</Location>
```

---

## Part 6: Validation & Testing (30 min)

### 6.1 Dispatcher Validator

```bash
# Validate dispatcher config
docker run --rm \
  -v $(pwd)/dispatcher/src:/src \
  adobe/dispatcher-validator:1.0.0 \
  /src

# Test with curl
curl -I https://www.example.com/content/dam/test.jpg
# Should return: Cache-Control: max-age=86400
```

### 6.2 Load Testing

```bash
# Test caching performance
hey -n 10000 -c 100 \
  -H "Host: www.example.com" \
  "https://www.example.com/content/dam/image.jpg"

# Test cache hit rate
# > 95% = good
# < 80% = needs optimization
```

### 6.3 AI Test Generation

```bash
goose run --task "Generate Dispatcher load tests:
1. Cache hit rate test
2. Concurrent request test
3. Security header validation
4. SSL/TLS validation
Output as JMeter test plan"
```

---

## Verification Checklist

- [ ] Dispatcher tests defined (TDD RED)
- [ ] Configuration implemented (TDD GREEN)
- [ ] Caching rules working
- [ ] Security headers set
- [ ] GraphQL proxy configured
- [ ] Validation tests passing
- [ ] Load tests completed

---

## BMAD Integration

| Phase | Activity |
|-------|----------|
| 01 | Define caching requirements |
| 02 | Model traffic patterns |
| 03 | Architecture for CDN/Dispatcher |
| 04 | Configure Dispatcher |
| 05 | Test performance & security |
| 06 | Monitor in production |

---

## Key Takeaways

1. **Cache aggressively** - Static assets should be cached
2. **Secure headers** - Always set CSP, HSTS
3. **Test validation** - Use dispatcher validator
4. **Monitor cache hit** - Track performance

---

## Next Steps

1. Configure CDN (Akamai/Cloudflare)
2. Set up SSL/TLS
3. Configure load balancer
4. Monitor with New Relic

---

## References

- [AEM Dispatcher](https://experienceleague.adobe.com/docs/experience-manager-dispatcher/using/dispatcher.html)
- [Dispatcher Configuration](https://experienceleague.adobe.com/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration.html)
