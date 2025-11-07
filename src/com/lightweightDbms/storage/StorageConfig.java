package com.lightweightDbms.storage;

import java.io.File;

/**
 * Configuration for file-based storage locations and delimiters.
 */
public final class StorageConfig {
    private final File rootDir;
    private final char fieldDelimiter;
    private final String escapeSequence;

    /**
     * @param rootPath base directory for all persisted data
     * @param fieldDelimiter delimiter for fields inside values
     * @param escapeSequence escape sequence for delimiter occurrences
     */
    public StorageConfig(String rootPath, char fieldDelimiter, String escapeSequence) {
        this.rootDir = new File(rootPath);
        if (!rootDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rootDir.mkdirs();
        }
        this.fieldDelimiter = fieldDelimiter;
        this.escapeSequence = escapeSequence == null ? "\\" : escapeSequence;
    }

    /**
     * @return root directory for storage
     */
    public File getRootDir() { return rootDir; }

    /**
     * @return delimiter used inside text fields to join structured data
     */
    public char getFieldDelimiter() { return fieldDelimiter; }

    /**
     * @return escape sequence used to escape delimiters inside values
     */
    public String getEscapeSequence() { return escapeSequence; }

    /**
     * @return users CSV file path
     */
    public File usersFile() { return new File(rootDir, "users.csv"); }

    /**
     * @return processed queries CSV file path
     */
    public File queriesFile() { return new File(rootDir, "queries.csv"); }

    /**
     * @return audit logs CSV file path
     */
    public File auditFile() { return new File(rootDir, "audit_logs.csv"); }

    /**
     * @param databaseName name of the single database
     * @return directory for database data
     */
    public File databaseDir(String databaseName) { return new File(new File(rootDir, "db"), databaseName); }

    /**
     * @param databaseName db name
     * @param table table name
     * @return CSV file for a table
     */
    public File tableFile(String databaseName, String table) { return new File(databaseDir(databaseName), table + ".csv"); }
}


