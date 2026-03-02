package com.demo.workflow.services.impl;

import com.demo.workflow.services.SecurityScannerService.SecurityFinding;
import com.demo.workflow.services.SecurityScannerService.Severity;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Advanced document security scanner for PDF and Office files.
 *
 * <p>Detects:</p>
 * <ul>
 *   <li>JavaScript in PDF files</li>
 *   <li>Macros in Office documents (OOXML)</li>
 *   <li>External links and references</li>
 *   <li>Embedded objects</li>
 *   <li>Auto-execute actions</li>
 * </ul>
 */
@Component(service = DocumentSecurityScanner.class, immediate = true)
public class DocumentSecurityScanner {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentSecurityScanner.class);

    // ═══════════════════════════════════════════════════════════════════
    // PDF Security Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final Pattern PDF_JAVASCRIPT = Pattern.compile(
        "/JavaScript|/JS\\s|/S\\s*/JavaScript", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_LAUNCH_ACTION = Pattern.compile(
        "/Launch|/Action\\s*/Launch", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_EMBEDDED_FILE = Pattern.compile(
        "/EmbeddedFile|/FileAttachment", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_OPEN_ACTION = Pattern.compile(
        "/OpenAction|/AA\\s", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_URI_ACTION = Pattern.compile(
        "/URI\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_ACROFORM = Pattern.compile(
        "/AcroForm|/XFA", Pattern.CASE_INSENSITIVE);
    private static final Pattern PDF_ENCRYPTED_STREAM = Pattern.compile(
        "/Encrypt|/Filter\\s*/Crypt", Pattern.CASE_INSENSITIVE);

    // ═══════════════════════════════════════════════════════════════════
    // Office (OOXML) Security Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final String VBA_PROJECT_FILE = "vbaProject.bin";
    private static final String MACRO_ENABLED_CONTENT_TYPE =
        "application/vnd.ms-office.vbaProject";

    private static final Pattern OFFICE_EXTERNAL_LINK = Pattern.compile(
        "r:link|r:id.*target.*http", Pattern.CASE_INSENSITIVE);
    private static final Pattern OFFICE_OLE_OBJECT = Pattern.compile(
        "oleObject|embeddedPackage", Pattern.CASE_INSENSITIVE);
    private static final Pattern OFFICE_ACTIVE_X = Pattern.compile(
        "activeX|objectEmbed", Pattern.CASE_INSENSITIVE);

    // ═══════════════════════════════════════════════════════════════════
    // Public Scanning Methods
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Scan a PDF file for security threats.
     */
    public List<SecurityFinding> scanPdf(InputStream content) {
        List<SecurityFinding> findings = new ArrayList<>();

        try {
            // Read PDF content (limited for memory safety)
            byte[] pdfBytes = readLimited(content, 10 * 1024 * 1024); // 10MB limit
            String pdfContent = new String(pdfBytes, StandardCharsets.ISO_8859_1);

            // Check for JavaScript
            if (PDF_JAVASCRIPT.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_JAVASCRIPT",
                    Severity.HIGH,
                    "JavaScript detected in PDF",
                    "PDF contains JavaScript which could execute malicious code",
                    "Remove JavaScript or review carefully before opening"
                ));
            }

            // Check for Launch actions
            if (PDF_LAUNCH_ACTION.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_LAUNCH_ACTION",
                    Severity.CRITICAL,
                    "Launch action detected in PDF",
                    "PDF contains launch action that could execute external programs",
                    "Remove launch action - this is a critical security risk"
                ));
            }

            // Check for embedded files
            if (PDF_EMBEDDED_FILE.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_EMBEDDED_FILE",
                    Severity.MEDIUM,
                    "Embedded file detected in PDF",
                    "PDF contains embedded files which could contain malware",
                    "Extract and scan embedded files separately"
                ));
            }

            // Check for OpenAction (auto-execute)
            if (PDF_OPEN_ACTION.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_OPEN_ACTION",
                    Severity.HIGH,
                    "Auto-execute action detected in PDF",
                    "PDF will automatically execute an action when opened",
                    "Remove OpenAction to prevent automatic execution"
                ));
            }

            // Check for external URIs
            Matcher uriMatcher = PDF_URI_ACTION.matcher(pdfContent);
            Set<String> suspiciousUrls = new HashSet<>();
            while (uriMatcher.find()) {
                String url = uriMatcher.group(1);
                if (isSuspiciousUrl(url)) {
                    suspiciousUrls.add(url);
                }
            }
            if (!suspiciousUrls.isEmpty()) {
                findings.add(createFinding(
                    "PDF_SUSPICIOUS_URI",
                    Severity.MEDIUM,
                    "Suspicious external links in PDF",
                    "PDF contains links to suspicious URLs: " + suspiciousUrls,
                    "Review all external links before publishing"
                ));
            }

            // Check for forms (AcroForm/XFA)
            if (PDF_ACROFORM.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_INTERACTIVE_FORM",
                    Severity.LOW,
                    "Interactive form detected in PDF",
                    "PDF contains interactive forms which could collect data",
                    "Ensure form submission targets are trusted"
                ));
            }

            // Check for encryption/obfuscation
            if (PDF_ENCRYPTED_STREAM.matcher(pdfContent).find()) {
                findings.add(createFinding(
                    "PDF_ENCRYPTED_CONTENT",
                    Severity.MEDIUM,
                    "Encrypted/obfuscated content in PDF",
                    "PDF contains encrypted streams which may hide malicious content",
                    "Decrypt and review content before publishing"
                ));
            }

            LOG.debug("PDF scan complete: {} findings", findings.size());

        } catch (IOException e) {
            LOG.error("Error scanning PDF", e);
            findings.add(createFinding(
                "PDF_SCAN_ERROR",
                Severity.MEDIUM,
                "Error scanning PDF",
                "Could not fully scan PDF: " + e.getMessage(),
                "Manual review recommended"
            ));
        }

        return findings;
    }

    /**
     * Scan an Office document (OOXML format) for security threats.
     */
    public List<SecurityFinding> scanOfficeDocument(InputStream content, String mimeType) {
        List<SecurityFinding> findings = new ArrayList<>();

        try {
            // OOXML files are ZIP archives
            byte[] zipBytes = readLimited(content, 50 * 1024 * 1024); // 50MB limit
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes));

            ZipEntry entry;
            boolean hasMacros = false;
            boolean hasExternalLinks = false;
            boolean hasOleObjects = false;
            boolean hasActiveX = false;
            List<String> suspiciousFiles = new ArrayList<>();

            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName().toLowerCase();

                // Check for VBA macros
                if (entryName.contains(VBA_PROJECT_FILE.toLowerCase()) ||
                    entryName.endsWith(".bin") && entryName.contains("vba")) {
                    hasMacros = true;
                    suspiciousFiles.add(entry.getName());
                }

                // Read XML content for further analysis
                if (entryName.endsWith(".xml") || entryName.endsWith(".rels")) {
                    String xmlContent = readZipEntry(zis);

                    if (OFFICE_EXTERNAL_LINK.matcher(xmlContent).find()) {
                        hasExternalLinks = true;
                    }
                    if (OFFICE_OLE_OBJECT.matcher(xmlContent).find()) {
                        hasOleObjects = true;
                    }
                    if (OFFICE_ACTIVE_X.matcher(xmlContent).find()) {
                        hasActiveX = true;
                    }

                    // Check content types for macro-enabled
                    if (entryName.equals("[content_types].xml")) {
                        if (xmlContent.contains(MACRO_ENABLED_CONTENT_TYPE)) {
                            hasMacros = true;
                        }
                    }
                }

                zis.closeEntry();
            }

            // Generate findings based on analysis
            if (hasMacros) {
                findings.add(createFinding(
                    "OFFICE_MACROS",
                    Severity.HIGH,
                    "VBA Macros detected in Office document",
                    "Document contains VBA macros which could execute malicious code. " +
                    "Files: " + String.join(", ", suspiciousFiles),
                    "Remove macros or thoroughly review VBA code before allowing"
                ));
            }

            if (hasExternalLinks) {
                findings.add(createFinding(
                    "OFFICE_EXTERNAL_LINKS",
                    Severity.MEDIUM,
                    "External links detected in Office document",
                    "Document contains links to external resources",
                    "Review all external links for safety"
                ));
            }

            if (hasOleObjects) {
                findings.add(createFinding(
                    "OFFICE_OLE_OBJECTS",
                    Severity.HIGH,
                    "OLE objects detected in Office document",
                    "Document contains embedded OLE objects which could contain malware",
                    "Extract and scan OLE objects separately"
                ));
            }

            if (hasActiveX) {
                findings.add(createFinding(
                    "OFFICE_ACTIVEX",
                    Severity.CRITICAL,
                    "ActiveX controls detected in Office document",
                    "Document contains ActiveX controls which can execute arbitrary code",
                    "Remove ActiveX controls - this is a critical security risk"
                ));
            }

            LOG.debug("Office document scan complete: {} findings", findings.size());

        } catch (IOException e) {
            LOG.error("Error scanning Office document", e);
            findings.add(createFinding(
                "OFFICE_SCAN_ERROR",
                Severity.MEDIUM,
                "Error scanning Office document",
                "Could not fully scan document: " + e.getMessage(),
                "Manual review recommended"
            ));
        }

        return findings;
    }

    /**
     * Check if a MIME type is a scannable document.
     */
    public boolean isScannableDocument(String mimeType) {
        if (mimeType == null) return false;

        String lower = mimeType.toLowerCase();
        return lower.equals("application/pdf") ||
               lower.contains("officedocument") ||
               lower.contains("msword") ||
               lower.contains("ms-excel") ||
               lower.contains("ms-powerpoint") ||
               lower.contains("opendocument");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Private Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private byte[] readLimited(InputStream input, int maxBytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int totalRead = 0;
        int bytesRead;

        while ((bytesRead = input.read(data)) != -1 && totalRead < maxBytes) {
            int toWrite = Math.min(bytesRead, maxBytes - totalRead);
            buffer.write(data, 0, toWrite);
            totalRead += toWrite;
        }

        return buffer.toByteArray();
    }

    private String readZipEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;
        int totalRead = 0;
        int maxSize = 1024 * 1024; // 1MB max per entry

        while ((bytesRead = zis.read(data)) != -1 && totalRead < maxSize) {
            buffer.write(data, 0, bytesRead);
            totalRead += bytesRead;
        }

        return buffer.toString(StandardCharsets.UTF_8.name());
    }

    private boolean isSuspiciousUrl(String url) {
        if (url == null) return false;

        String lower = url.toLowerCase();

        // Suspicious patterns
        return lower.contains("file://") ||
               lower.contains("javascript:") ||
               lower.contains("data:") ||
               lower.contains(".exe") ||
               lower.contains(".bat") ||
               lower.contains(".cmd") ||
               lower.contains(".ps1") ||
               lower.contains(".vbs") ||
               lower.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"); // IP addresses
    }

    private SecurityFinding createFinding(String category, Severity severity,
                                           String title, String description,
                                           String remediation) {
        return new SecurityFinding(
            UUID.randomUUID().toString(),
            severity,
            category,
            title + ": " + description,
            "document",
            category,
            mapToOwasp(category),
            mapToCwe(category)
        );
    }

    private String mapToOwasp(String category) {
        switch (category) {
            case "PDF_JAVASCRIPT":
            case "OFFICE_MACROS":
            case "OFFICE_ACTIVEX":
                return "A7:2017 - Cross-Site Scripting";
            case "PDF_LAUNCH_ACTION":
                return "A1:2017 - Injection";
            case "PDF_EMBEDDED_FILE":
            case "OFFICE_OLE_OBJECTS":
                return "A8:2017 - Insecure Deserialization";
            default:
                return "A6:2017 - Security Misconfiguration";
        }
    }

    private String mapToCwe(String category) {
        switch (category) {
            case "PDF_JAVASCRIPT":
            case "OFFICE_MACROS":
                return "CWE-94";  // Code Injection
            case "PDF_LAUNCH_ACTION":
            case "OFFICE_ACTIVEX":
                return "CWE-78";  // OS Command Injection
            case "PDF_EMBEDDED_FILE":
            case "OFFICE_OLE_OBJECTS":
                return "CWE-434"; // Dangerous File Upload
            default:
                return "CWE-693"; // Protection Mechanism Failure
        }
    }
}
