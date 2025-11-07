package com.lightweightDbms.audit;

/**
 * Provides a simulated IP address for audit events.
 */
public interface IpProvider {
    /**
     * @return an IPv4-like string
     */
    String getIp();
}


