package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.List;
import java.util.Objects;

/**
 * Command to update data in a table.
 */
public final class UpdateDataCommand implements Command {
    private final String tableName;
    private final List<String> columns;
    private final List<String> values;
    private final String whereClause;

    /**
     * @param tableName name of the table
     * @param columns columns to update
     * @param values new values
     * @param whereClause WHERE condition for update
     */
    public UpdateDataCommand(String tableName, List<String> columns, List<String> values, String whereClause) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.columns = Objects.requireNonNull(columns, "columns");
        this.values = Objects.requireNonNull(values, "values");
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
            return memEngine.getQuery().updateData(tableName, columns, values, whereClause);
        }
        return "Unsupported engine type.";
    }
}
