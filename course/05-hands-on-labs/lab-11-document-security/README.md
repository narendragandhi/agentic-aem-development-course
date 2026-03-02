# Lab 11: Document Security Scanner (PDF & Office)

## Objective
Build an advanced document security scanner that detects real security threats in PDF and Office documents, going beyond simple regex patterns to actual file parsing.

---

## Prerequisites
- Completed Lab 10 (Security Scanner)
- Understanding of ZIP file structures (OOXML)
- Familiarity with PDF internal structure

---

## Learning Outcomes
After completing this lab, you will be able to:
1. Parse PDF files and detect JavaScript, Launch actions, embedded files
2. Parse Office documents (DOCX, XLSX, PPTX) as ZIP archives
3. Detect VBA macros, ActiveX controls, and OLE objects
4. Integrate document scanning into the security workflow
5. Apply TDD to binary file parsing

---

## Why This Matters

Basic regex patterns miss sophisticated attacks:

| Attack Type | Regex Detection | Real Parsing |
|-------------|-----------------|--------------|
| PDF JavaScript | Partial | Full |
| Office Macros | File extension only | VBA code detection |
| Embedded executables | None | Full binary detection |
| Obfuscated payloads | None | Stream analysis |

---

## Part 1: Understanding Document Threats

### 1.1 PDF Security Threats

PDFs can contain:
- **JavaScript** - Executes on open, can exploit reader vulnerabilities
- **Launch Actions** - Execute external programs
- **Embedded Files** - Hide malware inside PDFs
- **OpenAction** - Auto-execute actions on document open
- **Encrypted Streams** - Hide malicious content from scanners

### 1.2 Office Document Threats

OOXML documents (.docx, .xlsx, .pptx) are ZIP archives containing:
- **vbaProject.bin** - VBA macro code
- **ActiveX controls** - Can execute arbitrary code
- **OLE objects** - Embedded executables
- **External links** - Data exfiltration, phishing

---

## Part 2: RED Phase - Write Specification Tests

### 2.1 Create Test File

Create `DocumentSecurityScannerSpec.java`:

```java
package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import com.demo.workflow.services.SecurityScannerService.Severity;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentSecurityScanner Specification")
class DocumentSecurityScannerSpec {

    private DocumentSecurityScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new DocumentSecurityScanner();
    }

    // ═══════════════════════════════════════════════════════════════════
    // PDF Scanning Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning PDF files")
    class PdfScanning {

        @Test
        @DisplayName("should detect JavaScript in PDF")
        void shouldDetectJavaScriptInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "<< /Type /Action /S /JavaScript /JS (alert('XSS');) >>\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_JAVASCRIPT")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.HIGH));
        }

        @Test
        @DisplayName("should detect Launch actions in PDF")
        void shouldDetectLaunchActionsInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "<< /Type /Action /S /Launch /F (cmd.exe) >>\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_LAUNCH_ACTION")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect embedded files in PDF")
        void shouldDetectEmbeddedFilesInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "<< /Type /Filespec /F (malware.exe) /EmbeddedFile 2 0 R >>\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_EMBEDDED_FILE")));
        }

        @Test
        @DisplayName("should return empty list for clean PDF")
        void shouldReturnEmptyForCleanPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Office Document Scanning Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When scanning Office documents")
    class OfficeScanning {

        @Test
        @DisplayName("should detect VBA macros in Office document")
        void shouldDetectVbaMacrosInOffice() throws IOException {
            byte[] docx = createMockOOXML("vbaProject.bin", "VBA macro content");

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("OFFICE_MACROS")));
        }

        @Test
        @DisplayName("should detect ActiveX controls in Office document")
        void shouldDetectActiveXInOffice() throws IOException {
            String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<document><activeX classId=\"{...}\"/></document>";
            byte[] docx = createMockOOXML("word/activeX/activeX1.xml", xmlContent);

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("OFFICE_ACTIVEX")));
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("should detect OLE objects in Office document")
        void shouldDetectOleObjectsInOffice() throws IOException {
            String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<document><oleObject progId=\"Package\"/></document>";
            byte[] docx = createMockOOXML("word/document.xml", xmlContent);

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("OFFICE_OLE_OBJECTS")));
        }
    }

    // Helper method
    private byte[] createMockOOXML(String entryName, String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
```

### 2.2 Run Tests (They Should Fail)

```bash
mvn test -pl core -Dtest=DocumentSecurityScannerSpec
```

**Expected:** Compilation errors (class doesn't exist yet)

---

## Part 3: GREEN Phase - Implement Scanner

### 3.1 Create the Interface

```java
package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import java.io.InputStream;
import java.util.List;

public interface DocumentSecurityScanner {
    List<SecurityFinding> scanPdf(InputStream content);
    List<SecurityFinding> scanOfficeDocument(InputStream content, String mimeType);
    boolean isScannableDocument(String mimeType);
}
```

### 3.2 Implement PDF Scanning

```java
@Component(service = DocumentSecurityScanner.class, immediate = true)
public class DocumentSecurityScannerImpl implements DocumentSecurityScanner {

    // PDF Detection Patterns
    private static final Pattern PDF_JAVASCRIPT = Pattern.compile(
        "/JavaScript|/JS\\s|/S\\s*/JavaScript", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_LAUNCH_ACTION = Pattern.compile(
        "/Launch|/Action\\s*/Launch", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_EMBEDDED_FILE = Pattern.compile(
        "/EmbeddedFile|/FileAttachment", Pattern.CASE_INSENSITIVE);

    @Override
    public List<SecurityFinding> scanPdf(InputStream content) {
        List<SecurityFinding> findings = new ArrayList<>();

        try {
            // Read PDF content with size limit
            byte[] pdfBytes = readLimited(content, 10 * 1024 * 1024);
            String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);

            // Check for JavaScript
            if (PDF_JAVASCRIPT.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_JAVASCRIPT",
                    Severity.HIGH,
                    "JavaScript detected in PDF",
                    "PDF contains JavaScript which could execute malicious code"
                ));
            }

            // Check for Launch actions
            if (PDF_LAUNCH_ACTION.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_LAUNCH_ACTION",
                    Severity.CRITICAL,
                    "Launch action detected in PDF",
                    "PDF can execute external programs"
                ));
            }

            // Check for embedded files
            if (PDF_EMBEDDED_FILE.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_EMBEDDED_FILE",
                    Severity.MEDIUM,
                    "Embedded file detected in PDF",
                    "PDF contains embedded files which could be malware"
                ));
            }

        } catch (IOException e) {
            LOG.error("Error scanning PDF", e);
        }

        return findings;
    }
}
```

### 3.3 Implement Office Document Scanning

```java
@Override
public List<SecurityFinding> scanOfficeDocument(InputStream content, String mimeType) {
    List<SecurityFinding> findings = new ArrayList<>();

    try {
        // OOXML files are ZIP archives
        byte[] zipBytes = readLimited(content, 50 * 1024 * 1024);
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));

        ZipEntry entry;
        boolean hasMacros = false;
        boolean hasActiveX = false;
        boolean hasOleObjects = false;

        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName().toLowerCase();

            // Check for VBA macros
            if (entryName.contains("vbaproject.bin")) {
                hasMacros = true;
            }

            // Read XML content for analysis
            if (entryName.endsWith(".xml")) {
                String xmlContent = readZipEntry(zis);

                if (xmlContent.contains("activeX")) {
                    hasActiveX = true;
                }
                if (xmlContent.contains("oleObject")) {
                    hasOleObjects = true;
                }
            }

            zis.closeEntry();
        }

        // Generate findings
        if (hasMacros) {
            findings.add(createFinding("OFFICE_MACROS", Severity.HIGH,
                "VBA Macros detected", "Document contains executable macros"));
        }
        if (hasActiveX) {
            findings.add(createFinding("OFFICE_ACTIVEX", Severity.CRITICAL,
                "ActiveX controls detected", "Can execute arbitrary code"));
        }
        if (hasOleObjects) {
            findings.add(createFinding("OFFICE_OLE_OBJECTS", Severity.HIGH,
                "OLE objects detected", "May contain embedded executables"));
        }

    } catch (IOException e) {
        LOG.error("Error scanning Office document", e);
    }

    return findings;
}
```

### 3.4 Run Tests

```bash
mvn test -pl core -Dtest=DocumentSecurityScannerSpec
```

**Expected:** All tests pass

---

## Part 4: Integration with SecurityScannerService

### 4.1 Add Document Scanner Reference

```java
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    private volatile DocumentSecurityScanner documentScanner;

    @Override
    public SecurityScanResult scanAsset(Asset asset) {
        // ... existing code ...

        // Add document scanning
        DocumentSecurityScanner docScanner = this.documentScanner;
        if (docScanner != null && docScanner.isScannableDocument(mimeType)) {
            try (InputStream content = asset.getOriginal().getStream()) {
                if (mimeType.equals("application/pdf")) {
                    allFindings.addAll(docScanner.scanPdf(content));
                } else {
                    allFindings.addAll(docScanner.scanOfficeDocument(content, mimeType));
                }
            }
        }

        // ... rest of method ...
    }
}
```

---

## Part 5: BEAD Task Integration

### 5.1 Create BEAD Task File

```yaml
task_id: DOCSEC-001
title: "Implement Document Security Scanner"
status: completed

tdd:
  spec_file: "DocumentSecurityScannerSpec.java"
  test_count: 19
  phase: green

build:
  files:
    - "DocumentSecurityScanner.java"
  integration:
    - "SecurityScannerServiceImpl.java"

security:
  threats_detected:
    - PDF JavaScript
    - PDF Launch Actions
    - PDF Embedded Files
    - Office VBA Macros
    - Office ActiveX
    - Office OLE Objects
```

---

## Verification Checklist

- [ ] All 19 specification tests pass
- [ ] PDF scanning detects: JavaScript, Launch, Embedded files, OpenAction
- [ ] Office scanning detects: Macros, ActiveX, OLE objects, External links
- [ ] Integration with SecurityScannerServiceImpl works
- [ ] BEAD task file created
- [ ] No false positives on clean documents

---

## Bonus Challenges

1. **Add PDF Form Detection:** Detect AcroForm/XFA forms
2. **Add Suspicious URL Detection:** Check for IP addresses, file:// URIs
3. **Add Encryption Detection:** Flag encrypted/obfuscated content
4. **Add Office External Link Detection:** Detect remote template injection

---

## References

- [PDF Reference Manual](https://www.adobe.com/devnet/pdf/pdf_reference.html)
- [OOXML Structure](https://docs.microsoft.com/en-us/openspecs/office_standards/ms-docx/)
- [PDF Security Best Practices](https://www.sans.org/reading-room/whitepapers/malicious/paper/35942)
