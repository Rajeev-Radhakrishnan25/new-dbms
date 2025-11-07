package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;
import com.lightweightDbms.transaction.Transaction;
import com.lightweightDbms.transaction.TransactionManager;

import java.util.Objects;

/**
 * Command to rollback the current transaction.
 */
public final class RollbackTransactionCommand implements Command {

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getTransactionManager() == null) {
                return "No transaction manager available.";
            }
            
            TransactionManager txnManager = memEngine.getTransactionManager();
            Transaction currentTxn = txnManager.getCurrentTransaction();
            if (currentTxn == null) {
                return "ERROR: No active transaction to rollback.";
            }
            
            String txnId = currentTxn.getTransactionId();
            boolean success = txnManager.rollbackTransaction(txnId);
            
            if (success) {
                return "Transaction '" + txnId + "' rolled back successfully.";
            } else {
                return "ERROR: Failed to rollback transaction '" + txnId + "'.";
            }
        }
        return "Unsupported engine type.";
    }
}
