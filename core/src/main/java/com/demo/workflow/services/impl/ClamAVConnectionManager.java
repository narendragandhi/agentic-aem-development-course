package com.demo.workflow.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Connection manager for ClamAV with retry logic and circuit breaker pattern.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                    CIRCUIT BREAKER STATE DIAGRAM                        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │     ┌──────────────────────────────────────────────────────────────┐   │
 * │     │                                                              │   │
 * │     │    ┌────────┐    failures >= threshold    ┌────────┐        │   │
 * │     │    │ CLOSED │ ─────────────────────────▶ │  OPEN  │        │   │
 * │     │    └────────┘                             └────────┘        │   │
 * │     │         ▲                                      │            │   │
 * │     │         │                                      │            │   │
 * │     │    success                             timeout elapsed      │   │
 * │     │         │                                      │            │   │
 * │     │         │         ┌───────────┐                │            │   │
 * │     │         └─────────│ HALF_OPEN │◀───────────────┘            │   │
 * │     │                   └───────────┘                             │   │
 * │     │                        │                                    │   │
 * │     │                   failure                                   │   │
 * │     │                        │                                    │   │
 * │     │                        └────────────────────────────────────┘   │
 * │     │                                (back to OPEN)                   │
 * │     └──────────────────────────────────────────────────────────────────┘   │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 */
public class ClamAVConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ClamAVConnectionManager.class);

    // Circuit breaker states
    public enum CircuitState {
        CLOSED,     // Normal operation
        OPEN,       // Failing, reject requests
        HALF_OPEN   // Testing if service recovered
    }

    // Configuration
    private final String host;
    private final int port;
    private final int connectionTimeout;
    private final int readTimeout;
    private final int maxRetries;
    private final long retryDelayMs;
    private final int failureThreshold;
    private final long circuitResetTimeoutMs;

    // Circuit breaker state
    private volatile CircuitState circuitState = CircuitState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong circuitOpenedTime = new AtomicLong(0);

    // Metrics
    private final AtomicInteger totalAttempts = new AtomicInteger(0);
    private final AtomicInteger successfulConnections = new AtomicInteger(0);
    private final AtomicInteger failedConnections = new AtomicInteger(0);

    public ClamAVConnectionManager(String host, int port, int connectionTimeout, int readTimeout) {
        this(host, port, connectionTimeout, readTimeout, 3, 1000, 5, 30000);
    }

    public ClamAVConnectionManager(String host, int port, int connectionTimeout, int readTimeout,
                                    int maxRetries, long retryDelayMs, int failureThreshold,
                                    long circuitResetTimeoutMs) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.failureThreshold = failureThreshold;
        this.circuitResetTimeoutMs = circuitResetTimeoutMs;
    }

    /**
     * Get a connection with retry logic and circuit breaker protection
     */
    public Socket getConnection() throws IOException {
        totalAttempts.incrementAndGet();

        // Check circuit breaker
        if (!isCircuitAllowingRequests()) {
            failedConnections.incrementAndGet();
            throw new IOException("Circuit breaker is OPEN - ClamAV service appears unavailable");
        }

        IOException lastException = null;
        int attempts = 0;

        while (attempts < maxRetries) {
            attempts++;

            try {
                Socket socket = createConnection();
                onSuccess();
                successfulConnections.incrementAndGet();
                return socket;

            } catch (IOException e) {
                lastException = e;
                LOG.warn("ClamAV connection attempt {}/{} failed: {}",
                    attempts, maxRetries, e.getMessage());

                if (attempts < maxRetries) {
                    try {
                        // Exponential backoff
                        long delay = retryDelayMs * (long) Math.pow(2, attempts - 1);
                        Thread.sleep(Math.min(delay, 10000)); // Cap at 10 seconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Connection interrupted", ie);
                    }
                }
            }
        }

        // All retries exhausted
        onFailure();
        failedConnections.incrementAndGet();
        throw new IOException("Failed to connect to ClamAV after " + maxRetries + " attempts", lastException);
    }

    /**
     * Create actual socket connection
     */
    private Socket createConnection() throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), connectionTimeout);
            socket.setSoTimeout(readTimeout);
            return socket;
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Ignore close exception
            }
            throw e;
        }
    }

    /**
     * Check if circuit breaker allows requests
     */
    private boolean isCircuitAllowingRequests() {
        switch (circuitState) {
            case CLOSED:
                return true;

            case OPEN:
                // Check if enough time has passed to try again
                long timeSinceOpened = System.currentTimeMillis() - circuitOpenedTime.get();
                if (timeSinceOpened >= circuitResetTimeoutMs) {
                    LOG.info("Circuit breaker transitioning from OPEN to HALF_OPEN");
                    circuitState = CircuitState.HALF_OPEN;
                    return true;
                }
                return false;

            case HALF_OPEN:
                return true;

            default:
                return true;
        }
    }

    /**
     * Called on successful connection
     */
    private void onSuccess() {
        if (circuitState == CircuitState.HALF_OPEN) {
            LOG.info("Circuit breaker recovered - transitioning to CLOSED");
            circuitState = CircuitState.CLOSED;
        }
        failureCount.set(0);
    }

    /**
     * Called on connection failure
     */
    private void onFailure() {
        lastFailureTime.set(System.currentTimeMillis());
        int failures = failureCount.incrementAndGet();

        if (circuitState == CircuitState.HALF_OPEN) {
            // Failed during half-open test, go back to open
            LOG.warn("Circuit breaker test failed - returning to OPEN state");
            circuitState = CircuitState.OPEN;
            circuitOpenedTime.set(System.currentTimeMillis());

        } else if (failures >= failureThreshold && circuitState == CircuitState.CLOSED) {
            LOG.warn("Circuit breaker OPENING after {} consecutive failures", failures);
            circuitState = CircuitState.OPEN;
            circuitOpenedTime.set(System.currentTimeMillis());
        }
    }

    /**
     * Test if ClamAV is reachable (for health checks)
     */
    public boolean testConnection() {
        try (Socket socket = createConnection()) {
            socket.getOutputStream().write("zPING\0".getBytes());
            socket.getOutputStream().flush();

            byte[] response = new byte[10];
            int bytesRead = socket.getInputStream().read(response);
            String result = new String(response, 0, bytesRead).trim();

            boolean success = "PONG".equals(result);
            if (success) {
                onSuccess();
            }
            return success;

        } catch (IOException e) {
            LOG.debug("ClamAV health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current circuit breaker state
     */
    public CircuitState getCircuitState() {
        return circuitState;
    }

    /**
     * Get failure count
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * Get connection statistics
     */
    public ConnectionStats getStats() {
        return new ConnectionStats(
            totalAttempts.get(),
            successfulConnections.get(),
            failedConnections.get(),
            circuitState,
            failureCount.get()
        );
    }

    /**
     * Reset circuit breaker (for testing/admin)
     */
    public void resetCircuitBreaker() {
        circuitState = CircuitState.CLOSED;
        failureCount.set(0);
        LOG.info("Circuit breaker manually reset to CLOSED");
    }

    /**
     * Statistics holder
     */
    public static class ConnectionStats {
        public final int totalAttempts;
        public final int successful;
        public final int failed;
        public final CircuitState circuitState;
        public final int currentFailureCount;

        public ConnectionStats(int totalAttempts, int successful, int failed,
                               CircuitState circuitState, int currentFailureCount) {
            this.totalAttempts = totalAttempts;
            this.successful = successful;
            this.failed = failed;
            this.circuitState = circuitState;
            this.currentFailureCount = currentFailureCount;
        }

        public double getSuccessRate() {
            return totalAttempts > 0 ? (double) successful / totalAttempts * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format(
                "ConnectionStats{attempts=%d, success=%d (%.1f%%), failed=%d, circuit=%s, failures=%d}",
                totalAttempts, successful, getSuccessRate(), failed, circuitState, currentFailureCount
            );
        }
    }
}
