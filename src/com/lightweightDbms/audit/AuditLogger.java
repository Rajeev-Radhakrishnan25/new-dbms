package com.lightweightDbms.audit;

import java.util.List;

/**
 * Contract for persisting and querying audit records.
 */
public interface AuditLogger {
    /**
     * Persists an audit record.
     *
     * @param record record to persist
     */
    void log(AuditRecord record);

    /**
     * Reads all audit records.
     *
     * @return list of records
     */
    List<AuditRecord> readAll();

    /**
     * Returns the most recent N records.
     *
     * @param limit max number of records
     * @return recent records in reverse chronological order
     */
    List<AuditRecord> recent(int limit);
}


