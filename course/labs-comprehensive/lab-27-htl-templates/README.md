# Lab 27: HTL Templates - Expert Level

## Objective
Master HTL (HTML Template Language) - AEM's modern server-side templating engine formerly known as Sightly.

---

## What You'll Learn
- HTL syntax and directives
- data-sly-use, data-sly-test, data-sly-list, data-sly-repeat
- Context-aware output
- Internationalization
- Custom template blocks
- Clientlibs integration

---

## Part 1: HTL Fundamentals

### HTL vs JSP

```
┌─────────────────────────────────────────────────────────────────┐
│                        HTL vs JSP                                │
├────────────────────────────┬────────────────────────────────────┤
│         HTL               │              JSP                    │
├────────────────────────────┼────────────────────────────────────┤
│ ${properties.title}       │ <%= properties.get("title", "") %> │
│ data-sly-use.model        │ <%@ page import="..." %>           │
│ Server-side only          │ Can write client JS                │
│ Safe by default          │ XSS vulnerable                      │
└────────────────────────────┴────────────────────────────────────┘
```

### Directives Overview

| Directive | Purpose | Example |
|-----------|---------|---------|
| `data-sly-use` | Use a class/HTL | `data-sly-use.model="MyModel"` |
| `data-sly-test` | Conditional | `data-sly-test="${showTitle}"` |
| `data-sly-list` | Loop | `data-sly-list="${items}"` |
| `data-sly-repeat` | Loop with key | `data-sly-repeat="${items}"` |
| `data-sly-resource` | Include | `data-sly-resource="${resource}"` |
| `data-sly-call` | Call template | `data-sly-call="${template.footer}"` |
| `data-sly-unwrap` | Remove wrapper | `<span data-sly-unwrap>` |

---

## Part 2: Basic HTL Templates

### Exercise 1: Display Properties

```html
<!-- component.html -->
<div class="component-title">
    <h1 data-sly-test="${properties.title}">${properties.title}</h1>
    <p data-sly-test="${properties.description}">${properties.description}</p>
</div>
```

### Exercise 2: Resource Properties

```html
<!-- Using resource instead of properties -->
<div class="page-info">
    <h1>${resource.metadata['dc:title']}</h1>
    <span>Created: ${resource.created @ fmt:date='yyyy-MM-dd'}</span>
</div>
```

### Exercise 3: Conditional Logic

```html
<!-- Show based on condition -->
<div class="asset-preview" 
     data-sly-test="${!properties.published && resource['jcr:content']['cq:lastReplicated']}">
    <span class="badge">Draft</span>
</div>

<div class="asset-preview published" 
     data-sly-test="${resource['jcr:content']['cq:lastReplicated']}">
    <span class="badge success">Published</span>
</div>
```

### Exercise 4: Loops

```html
<!-- data-sly-list -->
<ul data-sly-list="${resource.listChildren}">
    <li>${item.name}</li>
</ul>

<!-- With index -->
<ul data-sly-list.child="${resource.listChildren}">
    <li data-sly-list-item="${child}">${item.name}</li>
</ul>
```

### Exercise 5: Repeat with Keys

```html
<!-- data-sly-repeat (AEM 6.2+) -->
<div class="tags" data-sly-repeat="${resource.properties['cq:tags']}">
    <span class="tag">${item}</span>
</div>
```

---

## Part 3: Using Sling Models in HTL

### Exercise 6: Connect Sling Model

```html
<!-- assetViewer.html -->
<sly data-sly-use.assetInfo="com.demo.aem.models.AssetInfo">
    
    <article class="asset-viewer">
        <header>
            <h1>${assetInfo.title}</h1>
            <span class="meta">
                By ${assetInfo.author} | 
                ${assetInfo.fileSize @ format='{:bytes}'}
            </span>
        </header>
        
        <div class="content">
            <p>${assetInfo.description}</p>
        </div>
        
        <footer data-sly-test="${assetInfo.published}">
            <span class="published-date">
                Published: ${assetInfo.lastReplicated @ fmt:date='long'}
            </span>
        </footer>
    </article>
    
</sly>
```

### Exercise 7: Multiple Models

```html
<!-- workflowPanel.html -->
<sly data-sly-use.assetInfo="com.demo.aem.models.AssetInfo">
<sly data-sly-use.workflowInfo="com.demo.aem.models.WorkflowInfo">
    
    <div class="workflow-panel">
        <div class="asset-summary">
            <h2>${assetInfo.title}</h2>
            <span>${assetInfo.mimeType}</span>
        </div>
        
        <div class="workflow-status">
            <span class="status">${workflowInfo.status}</span>
            <span class="assignee">Assigned: ${workflowInfo.assignee}</span>
        </div>
        
        <div class="workflow-history" data-sly-list="${workflowInfo.history}">
            <div class="history-item">
                <span>${item.action}</span>
                <span>${item.user}</span>
                <span>${item.date @ fmt:date='short'}</span>
            </div>
        </div>
    </div>
    
</sly>
</sly>
```

---

## Part 4: Template Blocks

### Exercise 8: Define Template

```html
<!-- templates.html -->
<template data-sly-template.footer="${@ title='Default Title'}">
    <footer class="site-footer">
        <p>&copy; 2025 ${title}</p>
    </footer>
</template>

<template data-sly-template.breadcrumb="${@ items}">
    <nav class="breadcrumb">
        <ol data-sly-list="${items}">
            <li><a href="${item.url}">${item.label}</a></li>
        </ol>
    </nav>
</template>

<template data-sly-template.card="${@ title, description, image}">
    <div class="card">
        <img data-sly-test="${image}" src="${image}" alt="${title}"/>
        <h3>${title}</h3>
        <p>${description}</p>
    </div>
</template>
```

### Exercise 9: Call Template

```html
<!-- Use templates -->
<sly data-sly-call="${templates.footer @ title='My Site'}"></sly>

<sly data-sly-call="${templates.breadcrumb @ items=${breadcrumbItems}}"></sly>

<div class="cards">
    <sly data-sly-call="${templates.card @ 
        title='Card 1', 
        description='Description 1',
        image='/content/dam/image1.png'}">
    </sly>
</div>
```

---

## Part 5: Internationalization (i18n)

### Exercise 10: Translate Strings

```html
<!-- Using i18n -->
<h1>${properties.title @ i18n}</h1>

<p>${'Click to edit' @ i18n}</p>

<!-- With context -->
<span>${'Last Modified' @ i18n}: ${properties.lastModified}</span>

<!-- Pluralization -->
<span>${'${count} items' @ i18n, count=5}</span>
```

### Dictionary Files

```xml
<!-- /apps/myapp/i18n/en.json -->
{
    "Click to edit": "Click to edit",
    "Last Modified": "Last Modified",
    "${count} items": {
        "one": "${count} item",
        "other": "${count} items"
    }
}
```

---

## Part 6: Format Service

### Exercise 11: Format Dates

```html
<!-- Date formatting -->
<span>Created: ${properties.created @ fmt:date='yyyy-MM-dd'}</span>
<span>Full: ${properties.created @ fmt:date='long'}</span>
<span>Time: ${properties.created @ fmt:time='short'}</span>
```

### Exercise 12: Format Numbers

```html
<!-- Number formatting -->
<span>${properties.size @ fmt:number='integer'}</span>
<span>${properties.rating @ fmt:number='percent'}</span>
<span>${properties.price @ fmt:number='currency'} </span>
```

### Exercise 13: Bytes Format

```html
<!-- Custom format (bytes) -->
<span>Size: ${properties.size @ format='{:bytes}'}</span>
<!-- Output: 1.5 MB -->
```

---

## Part 7: Client Libraries

### Exercise 14: Include CSS/JS

```html
<!-- component.html -->
<sly data-sly-use.clientlib="/libs/granite/sling/include/clientlib">
    <sly data-sly-call="${clientlib.css @ categories=['myapp.components']}"/>
    <sly data-sly-call="${clientlib.js @ categories=['myapp.components']}"/>
</sly>
```

### Exercise 15: Async Loading

```html
<!-- Load JS asynchronously -->
<sly data-sly-resource="${'clientlib' @ resourceType='granite/sling/include/clientlib' 
    @ addTags=['myapp.async']}"></sly>
```

---

## Part 8: Complex Examples

### Exercise 16: Asset Detail Component

```html
<!-- asset-detail.html -->
<sly data-sly-use.asset="com.demo.aem.models.AssetDetail">
<sly data-sly-use WF="com.demo.aem.models.WorkflowActions">

<div class="asset-detail" data-asset-path="${resource.path}">
    
    <!-- Image Preview -->
    <div class="preview">
        <img src="${asset.renditionUrl}" 
             alt="${asset.title}"
             data-sly-test="${asset.hasImage}"/>
        <div class="placeholder" data-sly-test="${!asset.hasImage}">
            <span>${asset.mimeType}</span>
        </div>
    </div>
    
    <!-- Metadata -->
    <div class="metadata">
        <h1>${asset.title}</h1>
        
        <dl>
            <dt>Description</dt>
            <dd data-sly-test="${asset.description}">${asset.description}</dd>
            <dd data-sly-test="${!asset.description}">No description</dd>
            
            <dt>File Size</dt>
            <dd>${asset.formattedSize}</dd>
            
            <dt>Format</dt>
            <dd>${asset.mimeType}</dd>
            
            <dt>Created</dt>
            <dd>${asset.createdDate @ fmt:date='medium'}</dd>
            
            <dt>Author</dt>
            <dd>${asset.author}</dd>
        </dl>
    </div>
    
    <!-- Workflow Actions -->
    <div class="workflow-actions" 
         data-sly-test="${asset.activeWorkflow}">
        <h3>Workflow Actions</h3>
        <div class="status">
            <span class="workflow">${asset.workflowTitle}</span>
            <span class="step">${asset.currentStep}</span>
        </div>
        
        <div class="buttons" data-sly-list="${WF.actions}">
            <button class="${item.type}" 
                    data-action="${item.command}">
                ${item.label}
            </button>
        </div>
    </div>
    
    <!-- Versions -->
    <div class="versions" data-sly-test="${asset.hasVersions}">
        <h3>Version History</h3>
        <ul data-sly-list="${asset.versions}">
            <li>
                <span class="version">v${item.label}</span>
                <span class="date">${item.created @ fmt:date='short'}</span>
                <span class="user">${item.user}</span>
            </li>
        </ul>
    </div>
    
</div>

</sly>
</sly>
```

---

## Best Practices

| Practice | Do | Don't |
|----------|-----|-------|
| Logic | Use Sling Models | Don't use `<%` scriptlets |
| Output | Context-aware | Don't use `&{unsafe}` |
| I18n | Use i18n keys | Don't hardcode strings |
| JS | ClientLibs | Don't inline `<script>` |
| CSS | ClientLibs | Don't inline `<style>` |

---

## Verification Checklist

- [ ] Basic property display
- [ ] Conditional with data-sly-test
- [ ] Loops with data-sly-list
- [ ] Sling Model integration
- [ ] Template blocks defined
- [ ] Template calls
- [ ] Internationalization
- [ ] Date/number formatting
- [ ] ClientLibs inclusion

---

## Next Steps

- Lab 28: AEM Workflow Process Steps
- Lab 29: AEM Context Testing

---

## References

- [HTL Specification](https://experienceleague.adobe.com/docs/experience-manager-htl/using/htl.html)
- [HTL Block Expressions](https://experienceleague.adobe.com/docs/experience-manager-htl/using/htl/expressions.html)
