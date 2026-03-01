package com.demo.workflow.services;

/**
 * Service interface for integrating with an external system.
 */
public interface ExternalSystemIntegrationService {

    /**
     * Calls an external system API to get data.
     * @param payloadPath The path of the workflow payload.
     * @return A string response from the external system (e.g., in JSON format).
     */
    String getData(String payloadPath);
}
