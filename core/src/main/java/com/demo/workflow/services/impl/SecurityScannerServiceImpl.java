package com.demo.workflow.services.impl;

import com.day.cq.dam.api.Asset;
import com.demo.workflow.services.SecurityScannerService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of SecurityScannerService for content security analysis.
 *
 * <p>Provides security scanning capabilities including:</p>
 * <ul>
 *   <li>XSS detection in metadata</li>
 *   <li>SQL injection pattern detection</li>
 *   <li>File type validation (header vs extension)</li>
 *   <li>Embedded script detection in documents</li>
 * </ul>
 *
 * @see SecurityScannerService
 */
@Component(service = SecurityScannerService.class, immediate = true)
public class SecurityScannerServiceImpl implements SecurityScannerService {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityScannerServiceImpl.class);

    // ═══════════════════════════════════════════════════════════════════
    // XSS Detection Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<Pattern> XSS_PATTERNS = Arrays.asList(
        Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),  // onerror=, onload=, onclick=, etc.
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE)
    );

    // ═══════════════════════════════════════════════════════════════════
    // SQL Injection Detection Patterns
    // ═══════════════════════════════════════════════════════════════════

    private static final List<Pattern> SQL_INJECTION_PATTERNS = Arrays.asList(
        Pattern.compile("'\\s*OR\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("UNION\\s+SELECT", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DROP\\s+TABLE", Pattern.CASE_INSENSITIVE),
        Pattern.compile(";\\s*DROP\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("--\\s*$"),
        Pattern.compile("'\\s*;\\s*--", Pattern.CASE_INSENSITIVE)
    );

    // ═══════════════════════════════════════════════════════════════════
    // File Type Magic Bytes
    // ═══════════════════════════════════════════════════════════════════

    private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();
    private static final Map<String, String> EXTENSION_TO_MIME = new HashMap<>();

    static {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        MAGIC_BYTES.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        // JPEG: FF D8 FF
        MAGIC_BYTES.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        // GIF: 47 49 46 38
        MAGIC_BYTES.put("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        // PDF: 25 50 44 46 (%)
        MAGIC_BYTES.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});

        EXTENSION_TO_MIME.put("png", "image/png");
        EXTENSION_TO_MIME.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME.put("gif", "image/gif");
        EXTENSION_TO_MIME.put("pdf", "application/pdf");
        EXTENSION_TO_MIME.put("svg", "image/svg+xml");
        EXTENSION_TO_MIME.put("html", "text/html");
        EXTENSION_TO_MIME.put("htm", "text/html");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Dangerous File Extensions
    // ═══════════════════════════════════════════════════════════════════

    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
        "exe", "dll", "bat", "cmd", "ps1", "vbs", "js", "jar", "msi", "scr", "com"
    ));

    @Override
    public SecurityScanResult scanAsset(Asset asset) {
        long startTime = System.currentTimeMillis();
        List<SecurityFinding> allFindings = new ArrayList<>();

        try {
            // Scan metadata
            Map<String, Object> metadata = extractMetadata(asset);
            allFindings.addAll(scanMetadata(metadata));

            // Validate file type
            String mimeType = asset.getMimeType();
            String fileName = asset.getName();
            try (InputStream content = asset.getOriginal().getStream()) {
                FileTypeValidation validation = validateFileType(content, mimeType, fileName);
                if (!validation.isValid()) {
                    allFindings.add(createFileTypeValidationFinding(validation));
                }
            }

            // Scan for embedded scripts
            try (InputStream content = asset.getOriginal().getStream()) {
                allFindings.addAll(scanForEmbeddedScripts(content, mimeType));
            }

        } catch (Exception e) {
            LOG.error("Error scanning asset: {}", asset.getPath(), e);
        }

        long duration = System.currentTimeMillis() - startTime;
        Severity overallSeverity = calculateOverallSeverity(allFindings);
        boolean blocked = overallSeverity == Severity.CRITICAL || overallSeverity == Severity.HIGH;

        return new SecurityScanResult(allFindings, overallSeverity, blocked, duration);
    }

    @Override
    public List<SecurityFinding> scanMetadata(Map<String, Object> metadata) {
        List<SecurityFinding> findings = new ArrayList<>();

        if (metadata == null || metadata.isEmpty()) {
            return findings;
        }

        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String strValue = (String) value;

                // Check for XSS patterns
                findings.addAll(checkXssPatterns(key, strValue));

                // Check for SQL injection patterns
                findings.addAll(checkSqlInjectionPatterns(key, strValue));
            }
        }

        return findings;
    }

    @Override
    public FileTypeValidation validateFileType(InputStream content, String declaredMimeType, String fileName) {
        // Check for double extension attack
        if (hasDoubleExtension(fileName)) {
            return new FileTypeValidation(
                false,
                null,
                declaredMimeType,
                "Double extension attack detected: " + fileName,
                Severity.CRITICAL
            );
        }

        // Check for null byte injection
        if (hasNullByteInjection(fileName)) {
            return new FileTypeValidation(
                false,
                null,
                declaredMimeType,
                "Null byte injection detected: " + fileName,
                Severity.CRITICAL
            );
        }

        // Read file header to detect actual type
        String detectedMimeType = detectMimeTypeFromContent(content);

        // Validate header matches declared type
        if (detectedMimeType != null && !mimeTypesMatch(detectedMimeType, declaredMimeType)) {
            return new FileTypeValidation(
                false,
                detectedMimeType,
                declaredMimeType,
                "File header indicates " + detectedMimeType + " but declared as " + declaredMimeType,
                Severity.HIGH
            );
        }

        return new FileTypeValidation(true, detectedMimeType, declaredMimeType, null, null);
    }

    @Override
    public List<SecurityFinding> scanForEmbeddedScripts(InputStream content, String mimeType) {
        List<SecurityFinding> findings = new ArrayList<>();

        if (content == null || mimeType == null) {
            return findings;
        }

        // Only scan text-based formats that could contain scripts
        if (!isScriptCapableMimeType(mimeType)) {
            return findings;
        }

        try {
            String textContent = readStreamAsString(content);

            // Check for script tags
            if (containsScriptTag(textContent)) {
                findings.add(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.HIGH,
                    "EMBEDDED_SCRIPT",
                    "Embedded script tag detected in " + mimeType,
                    "document content",
                    "<script>",
                    "A7:2017 - Cross-Site Scripting (XSS)",
                    "CWE-79"
                ));
            }

            // Check for event handlers in SVG/HTML
            if (containsEventHandler(textContent)) {
                findings.add(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.HIGH,
                    "EMBEDDED_SCRIPT",
                    "Event handler attribute detected in " + mimeType,
                    "document content",
                    "on*=",
                    "A7:2017 - Cross-Site Scripting (XSS)",
                    "CWE-79"
                ));
            }

        } catch (IOException e) {
            LOG.warn("Error reading content for script scan", e);
        }

        return findings;
    }

    // ═══════════════════════════════════════════════════════════════════
    // Private Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private List<SecurityFinding> checkXssPatterns(String fieldName, String value) {
        List<SecurityFinding> findings = new ArrayList<>();

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                findings.add(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.CRITICAL,
                    "XSS",
                    "Potential XSS pattern detected in metadata field: " + fieldName,
                    fieldName,
                    pattern.pattern(),
                    "A7:2017 - Cross-Site Scripting (XSS)",
                    "CWE-79"
                ));
                break; // One finding per field is enough
            }
        }

        return findings;
    }

    private List<SecurityFinding> checkSqlInjectionPatterns(String fieldName, String value) {
        List<SecurityFinding> findings = new ArrayList<>();

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(value).find()) {
                findings.add(new SecurityFinding(
                    UUID.randomUUID().toString(),
                    Severity.CRITICAL,
                    "SQL_INJECTION",
                    "Potential SQL injection pattern detected in field: " + fieldName,
                    fieldName,
                    pattern.pattern(),
                    "A1:2017 - Injection",
                    "CWE-89"
                ));
                break; // One finding per field is enough
            }
        }

        return findings;
    }

    private boolean hasDoubleExtension(String fileName) {
        if (fileName == null) {
            return false;
        }

        // Look for patterns like .pdf.exe or .doc.bat
        String lowerName = fileName.toLowerCase();
        for (String dangerousExt : DANGEROUS_EXTENSIONS) {
            if (lowerName.endsWith("." + dangerousExt)) {
                // Check if there's another extension before the dangerous one
                String withoutDangerous = lowerName.substring(0, lowerName.lastIndexOf("." + dangerousExt));
                if (withoutDangerous.contains(".")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasNullByteInjection(String fileName) {
        if (fileName == null) {
            return false;
        }
        // Check for %00 or actual null byte
        return fileName.contains("%00") || fileName.contains("\u0000");
    }

    private String detectMimeTypeFromContent(InputStream content) {
        if (content == null) {
            return null;
        }

        try {
            byte[] header = new byte[8];
            int bytesRead = content.read(header);

            if (bytesRead < 3) {
                return null;
            }

            // Check against known magic bytes
            for (Map.Entry<String, byte[]> entry : MAGIC_BYTES.entrySet()) {
                byte[] magic = entry.getValue();
                if (bytesRead >= magic.length && startsWith(header, magic)) {
                    return entry.getKey();
                }
            }

        } catch (IOException e) {
            LOG.warn("Error reading file header for type detection", e);
        }

        return null;
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean mimeTypesMatch(String detected, String declared) {
        if (detected == null || declared == null) {
            return true; // Can't determine mismatch
        }
        return detected.equalsIgnoreCase(declared);
    }

    private boolean isScriptCapableMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String lower = mimeType.toLowerCase();
        return lower.contains("svg") ||
               lower.contains("html") ||
               lower.contains("xml") ||
               lower.contains("xhtml");
    }

    private String readStreamAsString(InputStream content) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(content, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private boolean containsScriptTag(String content) {
        if (content == null) {
            return false;
        }
        String lower = content.toLowerCase();
        return lower.contains("<script") || lower.contains("</script");
    }

    private boolean containsEventHandler(String content) {
        if (content == null) {
            return false;
        }
        // Match onload=, onerror=, onclick=, etc.
        return Pattern.compile("\\bon\\w+\\s*=", Pattern.CASE_INSENSITIVE)
                      .matcher(content)
                      .find();
    }

    private Map<String, Object> extractMetadata(Asset asset) {
        Map<String, Object> metadata = new HashMap<>();
        // In a real implementation, this would extract all metadata from the asset
        // For now, return basic properties
        if (asset.getMetadataValue("dc:title") != null) {
            metadata.put("dc:title", asset.getMetadataValue("dc:title"));
        }
        if (asset.getMetadataValue("dc:description") != null) {
            metadata.put("dc:description", asset.getMetadataValue("dc:description"));
        }
        return metadata;
    }

    private SecurityFinding createFileTypeValidationFinding(FileTypeValidation validation) {
        return new SecurityFinding(
            UUID.randomUUID().toString(),
            validation.getSeverity(),
            "FILE_TYPE_MISMATCH",
            validation.getMismatchReason(),
            "file header",
            "magic bytes",
            "A8:2017 - Insecure Deserialization",
            "CWE-434"
        );
    }

    private Severity calculateOverallSeverity(List<SecurityFinding> findings) {
        if (findings.isEmpty()) {
            return Severity.INFO;
        }

        return findings.stream()
            .map(SecurityFinding::getSeverity)
            .min(Comparator.comparingInt(this::severityOrder))
            .orElse(Severity.INFO);
    }

    private int severityOrder(Severity severity) {
        switch (severity) {
            case CRITICAL: return 0;
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
            case INFO: return 4;
            default: return 5;
        }
    }
}
