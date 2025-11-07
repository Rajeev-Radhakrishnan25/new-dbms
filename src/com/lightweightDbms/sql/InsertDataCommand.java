package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.List;
import java.util.Objects;

/**
 * Command to insert data into a table.
 */
public final class InsertDataCommand implements Command {
    private final String tableName;
    private final List<String> values;

    /**
     * @param tableName name of the table
     * @param values values to insert
     */
    public InsertDataCommand(String tableName, List<String> values) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.values = Objects.requireNonNull(values, "values");
    }

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getQuery() == null) {
                return "No query handler available.";
            }
            return memEngine.getQuery().insertData(tableName, values);
        }
        return "Unsupported engine type.";
    }
}
