package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;
import com.lightweightDbms.transaction.Transaction;
import com.lightweightDbms.transaction.TransactionManager;

import java.util.Objects;

/**
 * Command to commit the current transaction.
 */
public final class CommitTransactionCommand implements Command {

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
                return "ERROR: No active transaction to commit.";
            }
            
            String txnId = currentTxn.getTransactionId();
            boolean success = txnManager.commitTransaction(txnId);
            
            if (success) {
                return "Transaction '" + txnId + "' committed successfully.";
            } else {
                return "ERROR: Failed to commit transaction '" + txnId + "'.";
            }
        }
        return "Unsupported engine type.";
    }
}
