package com.lightweightDbms.sql;

import com.lightweightDbms.db.DatabaseEngine;

/**
 * Represents an executable SQL command.
 */
public interface Command {
    /**
     * Executes this command on the provided database engine.
     *
     * @param engine target database engine
     * @return human-readable result string
     * @throws IllegalArgumentException if the command cannot be executed
     */
    String execute(DatabaseEngine engine);
}


