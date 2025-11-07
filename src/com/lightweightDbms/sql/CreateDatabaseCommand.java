package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

import java.util.Objects;

/**
 * Command that creates the single allowed database.
 */
public final class CreateDatabaseCommand implements Command {
    private final String databaseName;

    /**
     * Creates a new instance.
     *
     * @param databaseName name to create
     */
    public CreateDatabaseCommand(String databaseName) {
        this.databaseName = Objects.requireNonNull(databaseName, "databaseName");
    }

    @Override
    public String execute(DatabaseEngine engine) {
        return engine.createDatabase(databaseName);
    }
}


