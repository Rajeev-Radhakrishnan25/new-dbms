package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;
import com.lightweightDbms.transaction.Transaction;
import com.lightweightDbms.transaction.TransactionManager;

import java.util.Objects;

/**
 * Command to begin a new transaction.
 */
public final class BeginTransactionCommand implements Command {

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getTransactionManager() == null) {
                return "No transaction manager available.";
            }
            
            TransactionManager txnManager = memEngine.getTransactionManager();
            if (txnManager.hasActiveTransaction()) {
                return "ERROR: There is already an active transaction. Commit or rollback first.";
            }
            
            Transaction transaction = txnManager.beginTransaction();
            return "Transaction '" + transaction.getTransactionId() + "' started.";
        }
        return "Unsupported engine type.";
    }
}
