package com.demo.workflow.services;

/**
 * A service for interacting with a Large Language Model (LLM).
 */
public interface LlmService {

    /**
     * Summarizes the given text using an LLM.
     * @param text The text to summarize.
     * @return The summary, or null if an error occurs.
     */
    String summarizeText(String text);
}
