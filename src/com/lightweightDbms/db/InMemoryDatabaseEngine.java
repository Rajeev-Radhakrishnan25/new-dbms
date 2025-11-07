package com.lightweightDbms.db;

import com.lightweightDbms.sql.Command;
import com.lightweightDbms.sql.Query;
import com.lightweightDbms.storage.StorageConfig;
import com.lightweightDbms.transaction.TransactionManager;

import java.util.Objects;

/**
 * Simple in-memory database engine that supports a single database lifecycle and
 * routes parsed commands for execution.
 */
public final class InMemoryDatabaseEngine implements DatabaseEngine {
    private String databaseName;
    private Query query;
    private TransactionManager transactionManager;

    /**
     * @param storage storage configuration for Query operations
     */
    public InMemoryDatabaseEngine(StorageConfig storage) {
        this.query = new Query(storage);
        this.transactionManager = new TransactionManager();
        this.query.setTransactionManager(transactionManager);
        this.transactionManager.setQuery(query);
    }

    /**
     * @return the Query instance for table operations
     */
    public Query getQuery() {
        return query;
    }

    /**
     * @return the TransactionManager instance
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public String createDatabase(String databaseName) {
        Objects.requireNonNull(databaseName, "databaseName");
        if (this.databaseName == null) {
            this.databaseName = databaseName;
            query.setCurrentDatabase(databaseName);
            return "Database '" + databaseName + "' created.";
        }
        if (!this.databaseName.equals(databaseName)) {
            throw new IllegalStateException("Only one database is allowed. Existing: " + this.databaseName);
        }
        return "Database '" + databaseName + "' already exists.";
    }

    @Override
    public boolean hasDatabase() {
        return this.databaseName != null;
    }

    @Override
    public String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public String execute(Command command) {
        Objects.requireNonNull(command, "command");
        return command.execute(this);
    }
}


