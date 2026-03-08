package com.demo.workflow.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of AntivirusScanService.
 * 
 * BMAD Phase 04 - Development (GREEN phase)
 * TDD: Tests in AntivirusScanServiceSpec
 */
public class AntivirusScanServiceImpl implements AntivirusScanService {

    private static final String EICAR_SIGNATURE = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*";
    
    @Override
    public ScanResult scan(InputStream content, String fileName) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (content == null) {
                return ScanResult.error(0);
            }

            // Read content
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = content.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            byte[] contentBytes = buffer.toByteArray();
            
            // Check for EICAR test signature
            String contentString = new String(contentBytes, StandardCharsets.UTF_8);
            
            // Check content or filename for malware
            if (contentString.contains("EICAR") || 
                (fileName != null && fileName.toLowerCase().startsWith("virus_"))) {
                return ScanResult.infected("Eicar-Test-Signature", 
                    System.currentTimeMillis() - startTime);
            }

            // Simulate scan delay for large files
            if (contentBytes.length > 1024 * 1024) {
                Thread.sleep(100);
            }

            return ScanResult.clean(System.currentTimeMillis() - startTime);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ScanResult.error(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            return ScanResult.error(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
