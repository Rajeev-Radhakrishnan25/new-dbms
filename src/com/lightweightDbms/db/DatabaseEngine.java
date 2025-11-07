package com.lightweightDbms.db;

import com.lightweightDbms.sql.Command;

/**
 * Database engine abstraction for a single in-memory database.
 *
 */
public interface DatabaseEngine {
    /**
     * Creates the single allowed database if it does not already exist.
     *
     * @param databaseName name of the database to create
     * @return message describing the result of the operation
     * @throws IllegalStateException if a database already exists and a different one is requested
     */
    String createDatabase(String databaseName);

    /**
     * Indicates whether the single database has been created.
     *
     * @return true if a database exists; otherwise, false
     */
    boolean hasDatabase();

    /**
     * Gets the current database name if present.
     *
     * @return the database name or null if not created
     */
    String getDatabaseName();

    /**
     * Executes a parsed command against the engine.
     *
     * @param command parsed command to execute
     * @return textual result to be displayed in the console
     * @throws IllegalArgumentException if the command is invalid for the current state
     */
    String execute(Command command);
}


