package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.Objects;

/**
 * Command to describe table structure.
 */
public final class DescribeTableCommand implements Command {
    private final String tableName;

    /**
     * @param tableName name of the table
     */
    public DescribeTableCommand(String tableName) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
    }

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getQuery() == null) {
                return "No query handler available.";
            }
            return memEngine.getQuery().describeTable(tableName);
        }
        return "Unsupported engine type.";
    }
}
