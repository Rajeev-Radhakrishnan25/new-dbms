package com.lightweightDbms.transaction;

import com.lightweightDbms.sql.Query;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages database transactions with ACID properties.
 * Provides thread-safe transaction handling with temporary storage.
 */
public final class TransactionManager {
    private final Map<String, Transaction> activeTransactions;
    private final Map<String, Transaction> completedTransactions;
    private Query query;

    /**
     * Creates a new transaction manager.
     */
    public TransactionManager() {
        this.activeTransactions = new ConcurrentHashMap<>();
        this.completedTransactions = new ConcurrentHashMap<>();
    }

    /**
     * Sets the query instance for applying transactions.
     * @param query query instance
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Begins a new transaction.
     * @return new transaction
     */
    public synchronized Transaction beginTransaction() {
        String transactionId = generateTransactionId();
        Transaction transaction = new Transaction(transactionId);
        activeTransactions.put(transactionId, transaction);
        return transaction;
    }

    /**
     * Commits a transaction, applying all changes to persistent storage.
     * @param transactionId transaction to commit
     * @return true if committed successfully
     */
    public synchronized boolean commitTransaction(String transactionId) {
        Transaction transaction = activeTransactions.get(transactionId);
        if (transaction == null) {
            return false;
        }

        transaction.getLock().lock();
        try {
            if (!transaction.isActive()) {
                return false;
            }

            transaction.setState(TransactionState.COMMITTING);
            
            // Apply all operations to persistent storage
            boolean success = applyOperations(transaction);
            
            if (success) {
                transaction.setState(TransactionState.COMMITTED);
                activeTransactions.remove(transactionId);
                completedTransactions.put(transactionId, transaction);
                return true;
            } else {
                transaction.setState(TransactionState.ROLLED_BACK);
                activeTransactions.remove(transactionId);
                completedTransactions.put(transactionId, transaction);
                return false;
            }
        } finally {
            transaction.getLock().unlock();
        }
    }

    /**
     * Rolls back a transaction, discarding all changes.
     * @param transactionId transaction to rollback
     * @return true if rolled back successfully
     */
    public synchronized boolean rollbackTransaction(String transactionId) {
        Transaction transaction = activeTransactions.get(transactionId);
        if (transaction == null) {
            return false;
        }

        transaction.getLock().lock();
        try {
            if (!transaction.isActive()) {
                return false;
            }

            transaction.setState(TransactionState.ROLLING_BACK);
            transaction.clearOperations();
            transaction.setState(TransactionState.ROLLED_BACK);
            
            activeTransactions.remove(transactionId);
            completedTransactions.put(transactionId, transaction);
            return true;
        } finally {
            transaction.getLock().unlock();
        }
    }

    /**
     * Gets an active transaction by ID.
     * @param transactionId transaction ID
     * @return transaction if active, null otherwise
     */
    public Transaction getActiveTransaction(String transactionId) {
        return activeTransactions.get(transactionId);
    }

    /**
     * Gets the current active transaction (if any).
     * @return current transaction or null
     */
    public Transaction getCurrentTransaction() {
        return activeTransactions.values().stream()
                .filter(Transaction::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if there's an active transaction.
     * @return true if there's an active transaction
     */
    public boolean hasActiveTransaction() {
        return !activeTransactions.isEmpty();
    }

    /**
     * Gets all active transactions.
     * @return list of active transactions
     */
    public java.util.List<Transaction> getActiveTransactions() {
        return new java.util.ArrayList<>(activeTransactions.values());
    }

    /**
     * Clears all completed transactions.
     */
    public synchronized void clearCompletedTransactions() {
        completedTransactions.clear();
    }

    /**
     * Gets transaction statistics.
     * @return statistics string
     */
    public String getStatistics() {
        return String.format("Active: %d, Completed: %d", 
                activeTransactions.size(), completedTransactions.size());
    }

    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean applyOperations(Transaction transaction) {
        if (query == null) {
            return false;
        }
        return query.applyTransaction(transaction);
    }
}
