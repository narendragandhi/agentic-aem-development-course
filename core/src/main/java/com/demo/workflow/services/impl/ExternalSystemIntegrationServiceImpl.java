package com.demo.workflow.services.impl;

import com.demo.workflow.services.ExternalSystemIntegrationService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Component(service = ExternalSystemIntegrationService.class)
public class ExternalSystemIntegrationServiceImpl implements ExternalSystemIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(ExternalSystemIntegrationServiceImpl.class);
    private static final String EXTERNAL_API_URL = "https://jsonplaceholder.typicode.com/posts/1";

    @Override
    public String getData(String payloadPath) {
        log.info("Calling external system for payload: {}", payloadPath);
        try {
            URL url = new URL(EXTERNAL_API_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                log.info("Received response from external system: {}", content.toString());
                return content.toString();
            } else {
                log.error("External system call failed with status: {}", status);
            }
        } catch (Exception e) {
            log.error("Exception while calling external system", e);
        }
        return null;
    }
}
