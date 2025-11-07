package com.lightweightDbms.audit;

import java.time.Instant;

/**
 * Immutable audit record describing a login/security event.
 */
public final class AuditRecord {
    private final long timestampMillis;
    private final String userId;
    private final String ipAddress;
    private final boolean success;
    private final String event;

    /**
     * @param timestampMillis epoch millis of event
     * @param userId user identifier (may be unknown/empty)
     * @param ipAddress simulated source IP address
     * @param success whether the attempt succeeded
     * @param event short event name, e.g., LOGIN
     */
    public AuditRecord(long timestampMillis, String userId, String ipAddress, boolean success, String event) {
        this.timestampMillis = timestampMillis;
        this.userId = userId == null ? "" : userId;
        this.ipAddress = ipAddress == null ? "" : ipAddress;
        this.success = success;
        this.event = event == null ? "" : event;
    }

    /**
     * @return epoch millis
     */
    public long getTimestampMillis() { return timestampMillis; }

    /**
     * @return user id
     */
    public String getUserId() { return userId; }

    /**
     * @return ip address string
     */
    public String getIpAddress() { return ipAddress; }

    /**
     * @return true if success
     */
    public boolean isSuccess() { return success; }

    /**
     * @return event name
     */
    public String getEvent() { return event; }

    @Override
    public String toString() {
        return "AuditRecord{" + Instant.ofEpochMilli(timestampMillis) + ", userId='" + userId + '\'' + ", ip='" + ipAddress + '\'' + ", success=" + success + ", event='" + event + "'}";
    }
}


