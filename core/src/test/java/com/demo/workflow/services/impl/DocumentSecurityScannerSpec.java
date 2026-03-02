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

/**
 * TDD Specification Tests for DocumentSecurityScanner.
 *
 * Tests PDF and Office document security scanning capabilities.
 */
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
                "1 0 obj\n" +
                "<< /Type /Catalog /OpenAction 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Action /S /JavaScript /JS (app.alert('XSS');) >>\n" +
                "endobj\n" +
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
                "1 0 obj\n" +
                "<< /Type /Action /S /Launch /F (cmd.exe) >>\n" +
                "endobj\n" +
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
                "1 0 obj\n" +
                "<< /Type /Filespec /F (malware.exe) /EmbeddedFile 2 0 R >>\n" +
                "endobj\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_EMBEDDED_FILE")));
        }

        @Test
        @DisplayName("should detect OpenAction auto-execute in PDF")
        void shouldDetectOpenActionInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /OpenAction << /S /URI /URI (http://evil.com) >> >>\n" +
                "endobj\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_OPEN_ACTION")));
        }

        @Test
        @DisplayName("should detect suspicious URLs in PDF")
        void shouldDetectSuspiciousUrlsInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Action /S /URI /URI (file:///etc/passwd) >>\n" +
                "endobj\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_SUSPICIOUS_URI")));
        }

        @Test
        @DisplayName("should detect AcroForm/XFA in PDF")
        void shouldDetectAcroFormInPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /AcroForm << /Fields [] >> >>\n" +
                "endobj\n" +
                "%%EOF";

            List<SecurityFinding> findings = scanner.scanPdf(
                new ByteArrayInputStream(pdfContent.getBytes(StandardCharsets.ISO_8859_1))
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("PDF_INTERACTIVE_FORM")));
        }

        @Test
        @DisplayName("should return empty list for clean PDF")
        void shouldReturnEmptyForCleanPdf() {
            String pdfContent = "%PDF-1.4\n" +
                "1 0 obj\n" +
                "<< /Type /Catalog /Pages 2 0 R >>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<< /Type /Pages /Kids [] /Count 0 >>\n" +
                "endobj\n" +
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
            assertTrue(findings.stream()
                .anyMatch(f -> f.getSeverity() == Severity.HIGH));
        }

        @Test
        @DisplayName("should detect external links in Office document")
        void shouldDetectExternalLinksInOffice() throws IOException {
            String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n" +
                "  <Relationship r:link=\"http://evil.com/payload\" Target=\"http://evil.com\"/>\n" +
                "</Relationships>";
            byte[] docx = createMockOOXML("_rels/.rels", xmlContent);

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("OFFICE_EXTERNAL_LINKS")));
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

        @Test
        @DisplayName("should detect ActiveX controls in Office document")
        void shouldDetectActiveXInOffice() throws IOException {
            String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<document><activeX classId=\"{00000000-0000-0000-0000-000000000000}\"/></document>";
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
        @DisplayName("should detect macro-enabled content type")
        void shouldDetectMacroEnabledContentType() throws IOException {
            String contentTypes = "<?xml version=\"1.0\"?>\n" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
                "  <Override ContentType=\"application/vnd.ms-office.vbaProject\"/>\n" +
                "</Types>";
            byte[] docx = createMockOOXML("[Content_Types].xml", contentTypes);

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.stream()
                .anyMatch(f -> f.getCategory().equals("OFFICE_MACROS")));
        }

        @Test
        @DisplayName("should return empty list for clean Office document")
        void shouldReturnEmptyForCleanOffice() throws IOException {
            String xmlContent = "<?xml version=\"1.0\"?>\n" +
                "<document><text>Hello World</text></document>";
            byte[] docx = createMockOOXML("word/document.xml", xmlContent);

            List<SecurityFinding> findings = scanner.scanOfficeDocument(
                new ByteArrayInputStream(docx),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );

            assertTrue(findings.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MIME Type Detection Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("When checking scannable document types")
    class MimeTypeDetection {

        @Test
        @DisplayName("should recognize PDF as scannable")
        void shouldRecognizePdfAsScannable() {
            assertTrue(scanner.isScannableDocument("application/pdf"));
        }

        @Test
        @DisplayName("should recognize Word documents as scannable")
        void shouldRecognizeWordAsScannable() {
            assertTrue(scanner.isScannableDocument(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            assertTrue(scanner.isScannableDocument("application/msword"));
        }

        @Test
        @DisplayName("should recognize Excel documents as scannable")
        void shouldRecognizeExcelAsScannable() {
            assertTrue(scanner.isScannableDocument(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            assertTrue(scanner.isScannableDocument("application/vnd.ms-excel"));
        }

        @Test
        @DisplayName("should recognize PowerPoint documents as scannable")
        void shouldRecognizePowerPointAsScannable() {
            assertTrue(scanner.isScannableDocument(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"));
            assertTrue(scanner.isScannableDocument("application/vnd.ms-powerpoint"));
        }

        @Test
        @DisplayName("should not recognize images as scannable documents")
        void shouldNotRecognizeImagesAsScannable() {
            assertFalse(scanner.isScannableDocument("image/png"));
            assertFalse(scanner.isScannableDocument("image/jpeg"));
        }

        @Test
        @DisplayName("should handle null MIME type")
        void shouldHandleNullMimeType() {
            assertFalse(scanner.isScannableDocument(null));
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Creates a mock OOXML (ZIP-based) document with a single entry.
     */
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
