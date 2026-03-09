# Lab 30: Production Dispatcher Configuration - Expert Level

## Objective
Master Apache Dispatcher configuration for AEM - critical for production performance and security.

---

## What You'll Learn
- Virtual host configuration
- Caching strategies
- Security headers
- Rewrite rules
- CORS configuration
- Load balancing
- Cache invalidation

---

## Part 1: Understanding Dispatcher

### Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      DISPATCHER ARCHITECTURE                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────┐      ┌─────────────┐      ┌─────────┐            │
│   │ Client  │─────▶│  Apache +   │─────▶│   AEM   │            │
│   │ Request │      │  Dispatcher │      │ Publish │            │
│   └─────────┘      └─────────────┘      └─────────┫            │
│                              │                   │              │
│                              ▼                   │              │
│                      ┌─────────────┐             │              │
│                      │    Cache    │◀────────────┤              │
│                      │  (Document) │  Invalidate │              │
│                      └─────────────┘             │              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Part 2: Virtual Host Configuration

### Exercise 1: Basic Virtual Host

```apache
# /etc/httpd/conf.d/enabled_vhosts/mysite.com.vhost

<VirtualHost *:80>
    ServerName mysite.com
    ServerAlias www.mysite.com
    
    # Logging
    CustomLog ${APACHE_LOG_DIR}/mysite.com_access.log combined
    ErrorLog ${APACHE_LOG_DIR}/mysite.com_error.log
    
    # Rewrite engine
    RewriteEngine On
    
    # Redirect www to non-www
    RewriteCond %{HTTP_HOST} ^www\.mysite\.com [NC]
    RewriteRule ^(.*)$ https://mysite.com$1 [R=301,L]
    
    # Include dispatcher configuration
    Include conf.d/dispatcher_vhost.conf
    
</VirtualHost>
```

### Exercise 2: SSL Virtual Host

```apache
# /etc/httpd/conf.d/enabled_vhosts/mysite.com_ssl.vhost

<VirtualHost *:443>
    ServerName mysite.com
    ServerAlias www.mysite.com
    
    # SSL Configuration
    SSLEngine On
    SSLCertificateFile /etc/ssl/certs/mysite.com.crt
    SSLCertificateKeyFile /etc/ssl/private/mysite.com.key
    SSLCertificateChainFile /etc/ssl/certs/DigiCertCA.crt
    
    # SSL Settings
    SSLProtocol all -SSLv3 -TLSv1 -TLSv1.1
    SSLCipherSuite HIGH:!aNULL:!MD5:!SEED:!IDEA
    SSLHonorCipherOrder on
    
    # Security Headers
    Header always set X-Frame-Options "SAMEORIGIN"
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-XSS-Protection "1; mode=block"
    Header always set Referrer-Policy "strict-origin-when-cross-origin"
    
    # Logging
    CustomLog ${APACHE_LOG_DIR}/mysite.com_ssl_access.log combined
    ErrorLog ${APACHE_LOG_DIR}/mysite.com_ssl_error.log
    
    # Include dispatcher
    Include conf.d/dispatcher_vhost.conf
    
</VirtualHost>
```

---

## Part 3: Dispatcher Configuration

### Exercise 3: Main Dispatcher Config

```apache
# /etc/httpd/conf.d/dispatcher_vhost.conf

# Load module
LoadModule dispatcher_module modules/mod_dispatcher.so

# Configuration file
DispatcherConfig conf/dispatcher.any

# Apache handler
DispatcherLog     logs/dispatcher.log 
DispatcherLogLevel debug

# Virtual host configuration
<VirtualHost *>
    # Enable dispatcher
    SetEnv DISP_ID default
    
    # Document root (not used with AEM but required)
    DocumentRoot /var/www/html
    
    # Enable dispatcher for this vhost
    <IfModule disp_apache2.c>
        SetHandler dispatcher-handler
    </IfModule>
    
    # MIME types
    TypesConfig /etc/mime.types
    
    # Security: Block access to JCR nodes
    <LocationMatch ".*/jcr:content.*">
        # Don't cache content
        SetHandler none
    </LocationMatch>
    
    # Block access to internal paths
    <Directory />
        Require all granted
    </Directory>
    
    # Allow public cache
    <DirectoryMatch "/mnt/var/dispatcher/cache">
        Require all granted
    </DirectoryMatch>
    
</VirtualHost>
```

### Exercise 4: Dispatcher Any Configuration

```any
# /usr/local/apache/conf/dispatcher.any

/farm {
    # Load balance configuration
    /clientheaders {
        # Headers to pass to AEM
        HTTP_HOST
        HTTP_X_FORWARDED_FOR
        HTTP_X_FORWARDED_PROTO
        HTTP_ACCEPT
        HTTP_ACCEPT_LANGUAGE
        HTTP_USER_AGENT
        HTTP_COOKIE
        HTTP_BASIC_AUTH
        CQ-ACTION
        CQ-HANDLE
        CQ-PATH
        CQ-PATHINFO
        CQ-SELECTOR
        CQ-EXT
        RESOLUTIONPATH
        REMOTE_USER
        AUTH_TYPE
    }

    /virtualhosts {
        # Virtual host patterns
        "mysite.com"
        "www.mysite.com"
        "*.mysite.com"
    }

    # Session management
    # /sessionmanagement {
    #     /directory "/mnt/var/sessions"
    #     /timeout "60"
    # }

    # Cache configuration
    /cache {
        # Root cache directory
        /docroot "/mnt/var/dispatcher/cache"

        # Default cache rules
        /rules {
            # Allow root
            /0001 { /type "allow" /url "/" }
            
            # HTML files - cache for 1 hour
            /0002 { /type "allow" /url "*.html" /age 3600 }
            
            # Static resources - cache long
            /0003 { /type "allow" /url "*.css" /age 86400 }
            /0004 { /type "allow" /url "*.js" /age 86400 }
            /0005 { /type "allow" /url "*.png" /age 604800 }
            /0006 { /type "allow" /url "*.jpg" /age 604800 }
            /0007 { /type "allow" /url "*.gif" /age 604800 }
            /0008 { /type "allow" /url "*.svg" /age 604800 }
            /0009 { /type "allow" /url "*.ico" /age 604800 }
            /0010 { /type "allow" /url "*.woff" /age 604800 }
            /0011 { /type "allow" /url "*.woff2" /age 604800 }
            /0012 { /type "allow" /url "*.ttf" /age 604800 }
            /0013 { /type "allow" /url "*.eot" /age 604800 }
            
            # Fonts
            /0020 { /type "allow" /url "/etc.clientlibs/*" /age 604800 }
            
            # Block internal paths
            /0099 { /type "deny" /url "/libs/*" }
            /0100 { /type "deny" /url "/apps/*" }
            /0101 { /type "deny" /url "/system/*" }
            /0102 { /type "deny" /url "/conf/*" }
            /0103 { /type "deny" /url "/home/*" }
            /0104 { /type "deny" /url "/admin/*" }
        }

        # Invalidation rules
        /invalidate {
            /0001 {
                /type "deny"
                /url "/libs/*"
            }
            /0002 {
                /type "deny"
                /apps/*"
            }
            /0003 {
                /type "allow"
                "/content/*"
            }
            /0004 {
                /type "allow"
                "/etc/*"
            }
        }

        # Allowed cache invalidation parameters
        /invalidateHandler "/dispatcher/invalidate.cache"

        # Statistics
        /statistics {
            /files {
                "/content/*"
                "/etc/*"
            }
        }

        # Grace period - serve stale while revalidating
        /gracePeriod "2"

        # Enable gzip compression
        /gzipPrefixedKeywords "vary"
    }

    # Backend configuration
    /backends {
        /aem_publish {
            /hostname "localhost"
            /port "4503"
            /timeout "10000"
            
            # Health check
            /healthcheck {
                /url "/system/health"
                /interval "30s"
                /timeout "5s"
            }
        }
    }

    # Failover configuration
    /failover {
        /timeout "5000"
        /retryCount "3"
    }

    # Request filtering
    /filter {
        # Allow selectors
        /0001 { /type "allow" /method "GET" /url "/content/*" }
        /0002 { /type "allow" /method "GET" /url "/etc/*" }
        /0003 { /type "allow" /method "GET" /url "/libs/*" }
        
        # Allowcq selectors and extensions
        /0010 { /type "allow" /method "GET" /url "*.html" }
        /0011 { /type "allow" /method "GET" /url "*.json" }
        /0012 { /type "allow" /method "GET" /url "*.css" }
        /0013 { /type "allow" /method "GET" /url "*.js" }
        /0014 { /type "allow" /method "GET" /url "*.png" }
        /0015 { /type "allow" /method "GET" /url "*.jpg" }
        /0016 { /type "allow" /method "GET" /url "*.gif" }
        
        # Form submissions
        /0020 { /type "allow" /method "POST" /url "/content/*" }
        
        # Deny all else
        /9999 { /type "deny" /url "*" }
    }

    # Rewrite rules
    /rewrites {
        # Include rewrite map
        /virtuallookups "conf/rewrites.example.com_rewrite.map"
        
        # Inline rewrites
        /rules {
            # Remove .html extension for clean URLs
            RewriteCond %{REQUEST_URI} \.html$
            RewriteRule ^/(.*)\.html$ /$1 [R=301,L]
            
            # Language redirect
            RewriteCond %{HTTP:Accept-Language} ^en [NC]
            RewriteRule ^/$ /en/ [R=301,L]
        }
    }
}
```

---

## Part 4: Security Configuration

### Exercise 5: Security Headers

```apache
# /etc/httpd/conf.d/security_headers.conf

# Protect against clickjacking
Header always set X-Frame-Options "SAMEORIGIN"

# Prevent MIME type sniffing
Header always set X-Content-Type-Options "nosniff"

# XSS Protection
Header always set X-XSS-Protection "1; mode=block"

# Referrer Policy
Header always set Referrer-Policy "strict-origin-when-cross-origin"

# Content Security Policy
Header always set Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;"

# Force HTTPS
Header always set Strict-Transport-Security "max-age=31536000; includeSubDomains"

# Remove server identification
ServerTokens Prod
ServerSignature Off
TraceEnable Off
```

### Exercise 6: CORS Configuration

```apache
# /etc/httpd/conf.d/cors.conf

<IfModule mod_headers.c>
    # GraphQL API CORS
    <LocationMatch "/graphql/execute.json.*">
        Header set Access-Control-Allow-Origin "https://mysite.com"
        Header set Access-Control-Allow-Methods "GET, POST, OPTIONS"
        Header set Access-Control-Allow-Headers "Content-Type, Authorization"
        Header set Access-Control-Max-Age "3600"
    </LocationMatch>
    
    # API CORS
    <LocationMatch "/api/.*">
        Header set Access-Control-Allow-Origin "*"
        Header set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
        Header set Access-Control-Allow-Headers "Content-Type, Authorization"
        
        # Handle preflight
        RewriteEngine On
        RewriteCond %{REQUEST_METHOD} OPTIONS
        RewriteRule ^(.*)$ $1 [R=200,L]
    </LocationMatch>
</IfModule>
```

---

## Part 5: Performance Configuration

### Exercise 7: Compression

```apache
# /etc/httpd/conf.d/compression.conf

<IfModule mod_deflate.c>
    # Compress HTML, CSS, JavaScript, Text, XML
    AddOutputFilterByType DEFLATE text/html text/plain text/xml text/css text/javascript application/javascript application/json
    
    # Compression level
    DeflateCompressionLevel 9
    
    # Exclude old browsers
    BrowserMatch ^Mozilla/4 gzip-only-text/html
    BrowserMatch ^Mozilla/4\.0[678] no-gzip
    BrowserMatch \bMSIE !no-gzip !gzip-only-text/html
</IfModule>

# Browser caching
<IfModule mod_expires.c>
    ExpiresActive On
    
    # Images - 1 year
    ExpiresByType image/jpg "access plus 1 year"
    ExpiresByType image/jpeg "access plus 1 year"
    ExpiresByType image/gif "access plus 1 year"
    ExpiresByType image/png "access plus 1 year"
    ExpiresByType image/svg+xml "access plus 1 year"
    
    # CSS/JS - 1 month
    ExpiresByType text/css "access plus 1 month"
    ExpiresByType application/javascript "access plus 1 month"
    
    # HTML - 1 hour
    ExpiresByType text/html "access plus 1 hour"
    
    # Default
    ExpiresDefault "access plus 2 days"
</IfModule>
```

### Exercise 8: Rate Limiting

```apache
# /etc/httpd/conf.d/ratelimiting.conf

<IfModule mod_ratelimit.c>
    # Rate limit settings
    SetOutputFilter RATE_LIMIT
    SetEnv rate-limit 512
</IfModule>

# Connection limiting
<IfModule mpm_prefork_module>
    StartServers             5
    MinSpareServers          5
    MaxSpareServers         10
    MaxRequestWorkers      150
    MaxConnectionsPerChild   0
</IfModule>
```

---

## Part 6: Cache Invalidation

### Exercise 9: Auto-Invalidation Script

```python
#!/usr/bin/env python3
"""
Cache invalidation script for AEM Dispatcher
"""

import os
import sys
import httplib

def invalidate_cache(host, path="/dispatcher/invalidate.cache"):
    """Invalidate dispatcher cache"""
    
    headers = {
        'CQ-Action': 'Activate',
        'CQ-Handle': '/content/mysite',
        'Content-Type': 'application/octet-stream'
    }
    
    conn = httplib.HTTPConnection(host, 8080)
    conn.request('POST', path, '', headers)
    response = conn.getresponse()
    
    if response.status == 200:
        print("Cache invalidated successfully")
        return True
    else:
        print(f"Error: {response.status} - {response.read()}")
        return False

def invalidate_by_path(host, content_path):
    """Invalidate specific content path"""
    
    headers = {
        'CQ-Action': 'Delete',
        'CQ-Path': content_path,
    }
    
    conn = httplib.HTTPConnection(host, 8080)
    conn.request('POST', '/dispatcher/invalidate.cache', '', headers)
    response = conn.getresponse()
    
    return response.status == 200

if __name__ == "__main__":
    if len(sys.argv) > 1:
        invalidate_by_path("localhost", sys.argv[1])
    else:
        invalidate_cache("localhost")
```

---

## Part 7: Health Check

### Exercise 10: Health Check Endpoint

```java
package com.demo.dispatcher;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Health check servlet for dispatcher load balancer
 */
@Component(
    service = SlingAllMethodsServlet.class,
    property = {
        "sling.servlet.paths=/system/health/dispatcher",
        "sling.servlet.methods=GET"
    }
)
public class DispatcherHealthCheckServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setStatus(SlingHttpServletResponse.SC_OK);
        
        response.getWriter().write("{\"status\":\"UP\",\"service\":\"dispatcher\"}");
    }
}
```

---

## Best Practices

| Practice | Recommendation |
|----------|----------------|
| Cache Static Content | Use aggressive caching for /etc.clientlibs |
| Deny Internal Paths | Always block /libs, /apps, /system |
| Use SSL | Always use HTTPS in production |
| Set Security Headers | X-Frame-Options, CSP, etc. |
| Monitor Logs | Watch dispatcher.log for errors |
| Grace Period | Use for smoother cache refresh |

---

## Verification Checklist

- [ ] Virtual host configured
- [ ] SSL enabled with modern ciphers
- [ ] Dispatcher caching configured
- [ ] Cache invalidation working
- [ ] Security headers set
- [ ] CORS configured
- [ ] Compression enabled
- [ ] Health check endpoint working
- [ ] Load balancing configured

---

## References

- [AEM Dispatcher Documentation](https://experienceleague.adobe.com/docs/experience-manager-dispatcher/using/dispatcher.html)
- [Dispatcher Configuration](https://experienceleague.adobe.com/docs/experience-manager-dispatcher/using/configuring/dispatcher-configuration.html)
