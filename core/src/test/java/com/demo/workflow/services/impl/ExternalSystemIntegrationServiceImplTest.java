package com.demo.workflow.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for ExternalSystemIntegrationServiceImpl.
 * Note: This test calls the real external API (jsonplaceholder.typicode.com).
 * In a production project, use WireMock or MockWebServer to mock HTTP calls.
 */
class ExternalSystemIntegrationServiceImplTest {

    private ExternalSystemIntegrationServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new ExternalSystemIntegrationServiceImpl();
    }

    @Test
    void testGetData() {
        // When - calls real external API
        String response = underTest.getData("/content/test");

        // Then - verify we got a valid JSON response
        assertNotNull(response, "Response should not be null");
        assertTrue(response.contains("userId"), "Response should contain userId field");
        assertTrue(response.contains("title"), "Response should contain title field");
        assertTrue(response.startsWith("{"), "Response should be valid JSON");
    }
}
