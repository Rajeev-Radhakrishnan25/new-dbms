package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.List;
import java.util.Objects;

/**
 * Command to select data from a table.
 */
public final class SelectDataCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final String whereClause;

    /**
     * @param tableName name of the table
     * @param columns columns to select
     * @param whereClause optional WHERE clause
     */
    public SelectDataCommand(String tableName, List<String> columns, String whereClause) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.columns = Objects.requireNonNull(columns, "columns");
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
            return memEngine.getQuery().selectData(tableName, columns, whereClause);
        }
        return "Unsupported engine type.";
    }
}
