package com.lightweightDbms.transaction;

/**
 * Represents the state of a transaction.
 */
public enum TransactionState {
    /** No active transaction */
    NONE,
    /** Transaction is active and accepting operations */
    ACTIVE,
    /** Transaction is being committed */
    COMMITTING,
    /** Transaction is being rolled back */
    ROLLING_BACK,
    /** Transaction has been committed */
    COMMITTED,
    /** Transaction has been rolled back */
    ROLLED_BACK
}
