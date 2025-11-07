package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.List;
import java.util.Objects;

/**
 * Command to create a new table with specified columns.
 */
public final class CreateTableCommand implements Command {
    private final String tableName;
    private final List<String> columns;

    /**
     * @param tableName name of the table
     * @param columns list of column definitions
     */
    public CreateTableCommand(String tableName, List<String> columns) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.columns = Objects.requireNonNull(columns, "columns");
    }

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getQuery() == null) {
                return "No query handler available.";
            }
            return memEngine.getQuery().createTable(tableName, columns);
        }
        return "Unsupported engine type.";
    }
}
