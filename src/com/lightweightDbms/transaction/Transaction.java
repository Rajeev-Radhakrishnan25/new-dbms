package com.lightweightDbms.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a database transaction with ACID properties.
 * Uses temporary storage to hold changes until commit/rollback.
 */
public final class Transaction {
    private final String transactionId;
    private final List<TransactionOperation> operations;
    private final ReentrantLock lock;
    private TransactionState state;
    private final long startTime;

    /**
     * Creates a new transaction.
     * @param transactionId unique transaction identifier
     */
    public Transaction(String transactionId) {
        this.transactionId = transactionId;
        this.operations = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.state = TransactionState.ACTIVE;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * @return transaction ID
     */
    public String getTransactionId() { return transactionId; }

    /**
     * @return current transaction state
     */
    public TransactionState getState() { return state; }

    /**
     * Sets the transaction state.
     * @param state new state
     */
    public void setState(TransactionState state) { this.state = state; }

    /**
     * @return list of operations in this transaction
     */
    public List<TransactionOperation> getOperations() { return new ArrayList<>(operations); }

    /**
     * @return transaction start time
     */
    public long getStartTime() { return startTime; }

    /**
     * @return transaction lock for thread safety
     */
    public ReentrantLock getLock() { return lock; }

    /**
     * Adds an operation to this transaction.
     * @param operation operation to add
     */
    public void addOperation(TransactionOperation operation) {
        if (state != TransactionState.ACTIVE) {
            throw new IllegalStateException("Cannot add operations to transaction in state: " + state);
        }
        operations.add(operation);
    }

    /**
     * @return true if transaction is active
     */
    public boolean isActive() {
        return state == TransactionState.ACTIVE;
    }

    /**
     * @return true if transaction is committed
     */
    public boolean isCommitted() {
        return state == TransactionState.COMMITTED;
    }

    /**
     * @return true if transaction is rolled back
     */
    public boolean isRolledBack() {
        return state == TransactionState.ROLLED_BACK;
    }

    /**
     * @return number of operations in this transaction
     */
    public int getOperationCount() {
        return operations.size();
    }

    /**
     * Clears all operations from this transaction.
     */
    public void clearOperations() {
        operations.clear();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + transactionId + '\'' +
                ", state=" + state +
                ", operations=" + operations.size() +
                ", startTime=" + startTime +
                '}';
    }
}
