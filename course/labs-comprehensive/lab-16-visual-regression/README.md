# Lab 16: Visual Regression Testing with Playwright
# Comprehensive Lab - 4 hours

## Objective

Implement visual regression testing for AEM workflows using Playwright. Detect unintended UI changes in workflow consoles, author UI, and published pages before they reach production.

---

## Prerequisites

- Lab 7 (Testing) completed
- Lab 8 (Deployment) completed
- Playwright installed
- AEM local instance running

---

## Overview

### What is Visual Regression Testing?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    VISUAL REGRESSION TESTING                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   Baseline Screenshots          Current Screenshots                        │
│   ───────────────────          ──────────────────                          │
│        ┌────────┐                   ┌────────┐                              │
│        │  Page  │                   │  Page  │                              │
│        │   A    │                   │   A'   │                              │
│        └────────┘                   └────────┘                              │
│             │                           │                                    │
│             │      Comparison           │                                    │
│             └───────────┬───────────────┘                                    │
│                         ▼                                                    │
│              ┌─────────────────────┐                                        │
│              │   Playwright/Vision  │                                        │
│              │      Engine          │                                        │
│              └──────────┬────────────┘                                        │
│                         │                                                    │
│            ┌────────────┴────────────┐                                       │
│            ▼                         ▼                                       │
│      ┌──────────┐              ┌──────────┐                                │
│      │  MATCH   │              │  DIFF    │                                │
│      │  ✓ Pass  │              │  ✗ Fail  │                                │
│      └──────────┘              └──────────┘                                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Tools

| Tool | Purpose |
|------|---------|
| Playwright | Browser automation |
| Playwright Screenshot | Built-in visual comparison |
| Percy | Cloud visual testing (optional) |
| Chromatic | Visual testing platform (optional) |

---

## Part 1: Playwright Setup (30 min)

### 1.1 Install Playwright

```bash
# Create test directory
mkdir -p tests/visual
cd tests/visual

# Initialize npm
npm init -y

# Install Playwright
npm install -D @playwright/test

# Install browsers
npx playwright install chromium
npx playwright install firefox
npx playwright install webkit
```

### 1.2 Create Playwright Configuration

Create `playwright.config.js`:

```javascript
// playwright.config.js
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],
    ['json', { outputFile: 'playwright-results.json' }]
  ],
  use: {
    baseURL: 'http://localhost:4502',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],
  webServer: {
    command: 'echo "AEM should be running"',
    url: 'http://localhost:4502',
    reuseExistingServer: !process.env.CI,
  },
});
```

### 1.3 Configure AEM Authentication

Create `tests/auth.spec.js`:

```javascript
// tests/auth.spec.js
const { test, expect } = require('@playwright/test');

test.describe('AEM Authentication', () => {
  test('should login to AEM', async ({ page }) => {
    await page.goto('/libs/granite/core/content/login.html');
    
    // Fill credentials
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin');
    await page.click('button[type="submit"]');
    
    // Verify logged in
    await expect(page).toHaveURL(/\/libs\/granite\/core\/content\/main\.html/);
  });
});
```

---

## Part 2: Workflow Console Tests (45 min)

### 2.1 Test Workflow Models Page

Create `tests/workflow-console.spec.js`:

```javascript
// tests/workflow-console.spec.js
const { test, expect } = require('@playwright/test');

test.describe('Workflow Console', () => {
  
  test.beforeEach(async ({ page }) => {
    // Login first
    await page.goto('/libs/granite/core/content/login.html');
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin');
    await page.click('button[type="submit"]');
    await page.waitForURL(/\/libs\/granite\/core\/content\/main\.html/);
  });

  test('workflow models page should match baseline', async ({ page }) => {
    await page.goto('/libs/cq/workflow/admin/console.html');
    
    // Wait for models to load
    await page.waitForSelector('.cq-workflow-models');
    
    // Take screenshot
    await expect(page).toHaveScreenshot('workflow-models.png', {
      maxDiffPixelRatio: 0.1,
    });
  });

  test('workflow initiation page should match baseline', async ({ page }) => {
    await page.goto('/libs/cq/workflow/admin/console.html?mode=initiation');
    
    await page.waitForSelector('.cq-workflow-initiations');
    
    await expect(page).toHaveScreenshot('workflow-initiation.png', {
      maxDiffPixelRatio: 0.1,
    });
  });
});
```

### 2.2 Test Asset Workflow

Create `tests/asset-workflow.spec.js`:

```javascript
// tests/asset-workflow.spec.js
const { test, expect } = require('@playwright/test');

test.describe('Asset Workflow', () => {
  
  test.beforeEach(async ({ page }) => {
    await page.goto('/libs/granite/core/content/login.html');
    await page.fill('#username', 'admin');
    await page.fill('#password', 'admin');
    await page.click('button[type="submit"]');
  });

  test('DAM asset upload dialog', async ({ page }) => {
    await page.goto('/assets.html');
    
    // Open upload dialog
    await page.click('button[data-foundation-command="create-upload"]');
    
    // Wait for upload UI
    await page.waitForSelector('.coral-FileUpload-input');
    
    await expect(page).toHaveScreenshot('asset-upload-dialog.png');
  });

  test('workflow inbox', async ({ page }) => {
    await page.goto('/inbox.html');
    
    await page.waitForSelector('.coral-Collection');
    
    await expect(page).toHaveScreenshot('workflow-inbox.png');
  });
});
```

---

## Part 3: Visual Comparison Configuration (30 min)

### 3.1 Screenshot Options

```javascript
// Advanced screenshot configuration
const screenshotOptions = {
  // Full page screenshot
  fullPage: true,
  
  // Mask elements (dynamic content)
  mask: [
    page.locator('.timestamp'),
    page.locator('.user-info'),
  ],
  
  // Specific viewport
  viewport: { width: 1280, height: 720 },
  
  // Maximum difference ratio (10%)
  maxDiffPixelRatio: 0.1,
  
  // Maximum different pixels
  maxDiffPixels: 500,
};
```

### 3.2 Ignore Dynamic Content

Create `tests/helpers/ignore-dynamic.js`:

```javascript
// tests/helpers/visual-utils.js
const { test as base } = require('@playwright/test');

// Custom test with visual comparison helpers
const visualTest = base.extend({
  page: async ({ page }, use) => {
    // Mask dynamic elements before screenshots
    await page.addStyleTag({
      content: `
        .timestamp, .date, .time, .created, .modified,
        .user-info, .user-name, .session-id,
        .random-id, [data-random] {
          visibility: hidden !important;
        }
      `
    });
    await use(page);
  }
});

module.exports = { visualTest };
```

### 3.3 Update Test to Use Masked Comparison

```javascript
// tests/workflow-console.spec.js (updated)
const { test, expect } = require('@playwright/test');

test('workflow models - baseline', async ({ page }) => {
  await page.goto('/libs/cq/workflow/admin/console.html');
  await page.waitForSelector('.cq-workflow-models');
  
  // Ignore dynamic content
  await page.locator('.timestamp, .user-info').evaluateAll(el => {
    el.forEach(e => e.style.visibility = 'hidden');
  });
  
  await expect(page).toHaveScreenshot('workflow-models.png', {
    mask: [
      page.locator('.timestamp'),
      page.locator('.user-info'),
    ],
    maxDiffPixelRatio: 0.05,
  });
});
```

---

## Part 4: CI/CD Integration (30 min)

### 4.1 GitHub Actions Workflow

Create `.github/workflows/visual-tests.yml`:

```yaml
name: Visual Regression Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  visual-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          
      - name: Install dependencies
        run: |
          npm ci
          npx playwright install --with-deps chromium
          
      - name: Start AEM (mock)
        run: |
          echo "Starting AEM mock for visual tests"
          # In real scenario: docker run -d -p 4502:4502 aem-author
          
      - name: Run visual tests
        run: npx playwright test --project=chromium
        
      - name: Upload visual diffs
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: visual-diffs
          path: |
            test-results/
            playwright-report/
            
      - name: Upload baseline screenshots
        if: github.event_name == 'push'
        uses: actions/upload-artifact@v4
        with:
          name: baselines
          path: tests/baselines/
          retention-days: 30
```

### 4.2 Update Baseline Screenshots

```bash
# Update baselines (run locally)
npx playwright test --update-snapshots

# Update for specific test
npx playwright test tests/workflow-console.spec.js --update-snapshots
```

---

## Part 5: Advanced Visual Testing (45 min)

### 5.1 Component-Level Testing

```javascript
// tests/components/workflow-step.spec.js
const { test, expect } = require('@playwright/test');

test.describe('Workflow Step Components', () => {
  
  test('participant dialog', async ({ page }) => {
    await page.goto('/libs/cq/workflow/admin/console.html');
    
    // Click on a workflow model
    await page.click('.cq-workflow-model-card:first-child');
    
    // Click add step
    await page.click('button[title="Add Step"]');
    
    // Select participant step
    await page.click('div[data-type="participant"]');
    
    // Wait for dialog
    await page.waitForSelector('.cq-workflow-participant-dialog');
    
    await expect(page.locator('.cq-workflow-participant-dialog'))
      .toHaveScreenshot('participant-step-dialog.png');
  });

  test('process step configuration', async ({ page }) => {
    await page.goto('/libs/cq/workflow/admin/console.html');
    await page.click('.cq-workflow-model-card:first-child');
    await page.click('button[title="Add Step"]');
    await page.click('div[data-type="process"]');
    
    await page.waitForSelector('.cq-workflow-process-dialog');
    
    await expect(page.locator('.cq-workflow-process-dialog'))
      .toHaveScreenshot('process-step-dialog.png');
  });
});
```

### 5.2 Responsive Testing

```javascript
// playwright.config.js - Add responsive projects
projects: [
  // Desktop
  { name: 'Desktop Chrome', use: { viewport: { width: 1920, height: 1080 } }},
  { name: 'Desktop Safari', use: { viewport: { width: 1440, height: 900 }}},
  
  // Tablet
  { name: 'iPad Pro', use: { viewport: { width: 1024, height: 1366 }}},
  { name: 'iPad Mini', use: { viewport: { width: 768, height: 1024 }}},
  
  // Mobile
  { name: 'iPhone 14', use: { viewport: { width: 390, height: 844 }}},
  { name: 'Pixel 7', use: { viewport: { width: 412, height: 915 }}},
]
```

### 5.3 Theme Testing (Dark/Light)

```javascript
// tests/accessibility/theme.spec.js
const { test, expect } = require('@playwright/test');

test.describe('Theme Testing', () => {
  
  test('light theme workflow console', async ({ page }) => {
    // Set light theme
    await page.addInitScript(() => {
      window.localStorage.setItem('coral-theme', 'light');
    });
    
    await page.goto('/libs/cq/workflow/admin/console.html');
    await expect(page).toHaveScreenshot('workflow-console-light.png');
  });
  
  test('dark theme workflow console', async ({ page }) => {
    // Set dark theme
    await page.addInitScript(() => {
      window.localStorage.setItem('coral-theme', 'dark');
    });
    
    await page.goto('/libs/cq/workflow/admin/console.html');
    await expect(page).toHaveScreenshot('workflow-console-dark.png');
  });
});
```

---

## Part 6: Percy Integration (Optional - 30 min)

### 6.1 Install Percy

```bash
npm install --save-dev @percy/cli @percy/playwright
```

### 6.2 Configure Percy

```javascript
// playwright.config.js
const { defineConfig } = require('@playwright/test');
const { percy } = require('@percy/playwright');

module.exports = defineConfig({
  // ... other config
  
  use: {
    percyCaptureBodies: 'visible',
  },
});
```

### 6.3 Percy Test

```javascript
// tests/percy-workflow.spec.js
const { test, expect } = require('@playwright/test');
const { percySnapshot } = require('@percy/playwright');

test('workflow console - Percy', async ({ page }) => {
  await page.goto('/libs/cq/workflow/admin/console.html');
  await page.waitForSelector('.cq-workflow-models');
  
  await percySnapshot(page, 'Workflow Console');
});
```

### 6.4 Run Percy

```bash
# Local
npx percy exec -- playwright test

# CI (requires PERCY_TOKEN)
npx percy exec -- github-action
```

---

## Verification Checklist

- [ ] Playwright installed and configured
- [ ] Authentication tests passing
- [ ] Workflow console screenshots captured
- [ ] Baseline screenshots stored
- [ ] Dynamic content masked
- [ ] CI workflow configured
- [ ] Percy integration working (if using)
- [ ] Mobile/responsive tests passing
- [ ] Theme tests implemented

---

## Key Takeaways

1. **Visual testing catches what unit tests miss** - UI changes, CSS regressions
2. **Mask dynamic content** - Timestamps, user info, random IDs
3. **Set appropriate thresholds** - Don't fail on 1-pixel differences
4. **Store baselines in Git** - Track visual changes over time
5. **Use Percy/Chromatic for teams** - Cloud-based visual testing at scale

---

## Next Steps

1. Add visual tests to all critical user flows
2. Integrate with AEM Cloud Manager pipelines
3. Set up Percy project for visual review
4. Configure Slack notifications for visual diffs
5. Review visual changes in PRs before merging

---

## References

- [Playwright Visual Comparisons](https://playwright.dev/docs/test-snapshots)
- [Playwright Screenshot API](https://playwright.dev/docs/api/class-page#page-screenshot)
- [Percy Documentation](https://docs.percy.io/docs)
- [Chromatic Documentation](https://www.chromatic.com/docs/)
