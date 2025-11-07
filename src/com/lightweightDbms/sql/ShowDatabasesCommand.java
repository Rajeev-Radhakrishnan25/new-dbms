package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.Objects;

/**
 * Command to show all tables in the current database.
 */
public final class ShowDatabasesCommand implements Command {

    @Override
    public String execute(DatabaseEngine engine) {
        Objects.requireNonNull(engine, "engine");
        if (engine instanceof com.lightweightDbms.db.InMemoryDatabaseEngine) {
            com.lightweightDbms.db.InMemoryDatabaseEngine memEngine = (com.lightweightDbms.db.InMemoryDatabaseEngine) engine;

            var database = memEngine.getDatabaseName();
            if (database == null) {
                return "No databases found.";
            }
            return "Databases:\n" + String.join("\n", database);
        }
        return "Unsupported engine type.";
    }
}
