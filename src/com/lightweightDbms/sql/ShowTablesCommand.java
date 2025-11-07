package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.Objects;

/**
 * Command to show all tables in the current database.
 */
public final class ShowTablesCommand implements Command {

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;
            if (memEngine.getQuery() == null) {
                return "No query handler available.";
            }
            var tables = memEngine.getQuery().showTables();
            if (tables.isEmpty()) {
                return "No tables found.";
            }
            return "Tables:\n" + String.join("\n", tables);
        }
        return "Unsupported engine type.";
    }
}
