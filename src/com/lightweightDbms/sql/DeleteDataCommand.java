package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.Objects;

/**
 * Command to delete data from a table.
 */
public final class DeleteDataCommand implements Command {
    private final String tableName;
    private final String whereClause;

    /**
     * @param tableName name of the table
     * @param whereClause WHERE condition for deletion
     */
    public DeleteDataCommand(String tableName, String whereClause) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.whereClause = whereClause;
    }

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getQuery() == null) {
                return "No query handler available.";
            }
            return memEngine.getQuery().deleteData(tableName, whereClause);
        }
        return "Unsupported engine type.";
    }
}
