package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

/**
 * Command to validate using the single database.
 */
public final class UseDatabaseCommand implements Command {
    private final String databaseName;

    /**
     * @param databaseName expected database name
     */
    public UseDatabaseCommand(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public String execute(DatabaseEngine engine) {
        if (!engine.hasDatabase()) {
            throw new IllegalArgumentException("No database exists. Create one first.");
        }
        if (engine.getDatabaseName() != null && !engine.getDatabaseName().equals(databaseName)) {
            throw new IllegalArgumentException("Only one database allowed: '" + engine.getDatabaseName() + "'.");
        }
        return "Using database '" + engine.getDatabaseName() + "'.";
    }
}


