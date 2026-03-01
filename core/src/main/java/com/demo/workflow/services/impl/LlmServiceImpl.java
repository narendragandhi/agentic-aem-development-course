package com.demo.workflow.services.impl;

import com.demo.workflow.services.LlmService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub implementation of LlmService.
 * This is a placeholder that can be replaced with a real LLM integration.
 */
@Component(service = LlmService.class)
public class LlmServiceImpl implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmServiceImpl.class);

    @Activate
    protected void activate() {
        log.info("LlmService activated (stub implementation)");
    }

    @Override
    public String summarizeText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Stub implementation: return first 200 characters as a simple "summary"
        log.info("LlmService.summarizeText called (stub implementation)");
        int maxLength = Math.min(text.length(), 200);
        String summary = text.substring(0, maxLength);
        if (text.length() > maxLength) {
            summary += "...";
        }
        return summary;
    }
}
