package com.lightweightDbms.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Minimal SQL parser that translates user input into {@link Command} instances.
 */
public final class SqlParser {

    /**
     * Parses a SQL-like input line into a {@link Command}.
     * Supported forms:
     * - CREATE DATABASE <name>;
     * - USE <name>;
     * - SHOW TABLES;
     * - CREATE TABLE <name> (<columns>);
     * - DESCRIBE <table>;
     * - SELECT <columns> FROM <table>;
     * - INSERT INTO <table> VALUES (<values>);
     * - EXIT; or QUIT; to terminate the session loop (handled by caller)
     *
     * @param line raw user input
     * @return a command instance or null if the line is a session control command
     * @throws IllegalArgumentException when the syntax is invalid
     */
    public Command parse(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }
        // Normalize
        String noSemicolon = trimmed.endsWith(";") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        String upper = noSemicolon.toUpperCase();

        if ("EXIT".equals(upper) || "QUIT".equals(upper)) {
            return null; // caller ends loop
        }

        if (upper.startsWith("CREATE DATABASE ")) {
            String name = noSemicolon.substring("CREATE DATABASE ".length()).trim();
            validateIdentifier(name);
            return new CreateDatabaseCommand(name);
        }

        if (upper.startsWith("USE ")) {
            String name = noSemicolon.substring("USE ".length()).trim();
            validateIdentifier(name);
            return new UseDatabaseCommand(name);
        }

        if ("SHOW DATABASES".equals(upper)) {
            return new ShowDatabasesCommand();
        }

        if ("SHOW TABLES".equals(upper)) {
            return new ShowTablesCommand();
        }

        if (upper.startsWith("CREATE TABLE ")) {
            String rest = noSemicolon.substring("CREATE TABLE ".length()).trim();
            int parenStart = rest.indexOf('(');
            int parenEnd = rest.lastIndexOf(')');
            if (parenStart == -1 || parenEnd == -1 || parenEnd <= parenStart) {
                throw new IllegalArgumentException("Invalid CREATE TABLE syntax. Use: CREATE TABLE name (col1, col2, ...)");
            }
            String tableName = rest.substring(0, parenStart).trim();
            String columnsStr = rest.substring(parenStart + 1, parenEnd).trim();
            validateIdentifier(tableName);
            List<String> columns = parseColumnList(columnsStr);
            return new CreateTableCommand(tableName, columns);
        }

        if (upper.startsWith("DESCRIBE ")) {
            String tableName = noSemicolon.substring("DESCRIBE ".length()).trim();
            validateIdentifier(tableName);
            return new DescribeTableCommand(tableName);
        }

        if (upper.startsWith("SELECT ")) {
            String rest = noSemicolon.substring("SELECT ".length()).trim();
            if (!upper.contains(" FROM ")) {
                throw new IllegalArgumentException("Invalid SELECT syntax. Use: SELECT columns FROM table");
            }
            String[] parts = rest.split(" FROM ", 2);
            String columnsStr = parts[0].trim();
            String tableName = parts[1].trim();
            validateIdentifier(tableName);
            List<String> columns = parseColumnList(columnsStr);
            return new SelectDataCommand(tableName, columns, null);
        }

        if (upper.startsWith("INSERT INTO ")) {
            String rest = noSemicolon.substring("INSERT INTO ".length()).trim();
            if (!upper.contains(" VALUES ")) {
                throw new IllegalArgumentException("Invalid INSERT syntax. Use: INSERT INTO table VALUES (val1, val2, ...)");
            }
            String[] parts = rest.split(" VALUES ", 2);
            String tableName = parts[0].trim();
            String valuesStr = parts[1].trim();
            if (!valuesStr.startsWith("(") || !valuesStr.endsWith(")")) {
                throw new IllegalArgumentException("Invalid VALUES syntax. Use: VALUES (val1, val2, ...)");
            }
            valuesStr = valuesStr.substring(1, valuesStr.length() - 1);
            validateIdentifier(tableName);
            List<String> values = parseValueList(valuesStr);
            return new InsertDataCommand(tableName, values);
        }

        if (upper.startsWith("DELETE FROM ")) {
            String rest = noSemicolon.substring("DELETE FROM ".length()).trim();
            String tableName;
            String whereClause = null;
            if (upper.contains(" WHERE ")) {
                String[] parts = rest.split(" WHERE ", 2);
                tableName = parts[0].trim();
                whereClause = parts[1].trim();
            } else {
                tableName = rest;
            }
            validateIdentifier(tableName);
            return new DeleteDataCommand(tableName, whereClause);
        }

        if (upper.startsWith("UPDATE ")) {
            String rest = noSemicolon.substring("UPDATE ".length()).trim();
            if (!upper.contains(" SET ")) {
                throw new IllegalArgumentException("Invalid UPDATE syntax. Use: UPDATE table SET col1=val1, col2=val2 WHERE condition");
            }
            String[] parts = rest.split(" SET ", 2);
            String tableName = parts[0].trim();
            String setClause = parts[1].trim();
            
            String whereClause = null;
            if (upper.contains(" WHERE ")) {
                String[] whereParts = setClause.split(" WHERE ", 2);
                setClause = whereParts[0].trim();
                whereClause = whereParts[1].trim();
            }
            
            validateIdentifier(tableName);
            List<String> columns = new ArrayList<>();
            List<String> values = new ArrayList<>();
            parseSetClause(setClause, columns, values);
            return new UpdateDataCommand(tableName, columns, values, whereClause);
        }

        // Transaction commands
        if ("BEGIN TRANSACTION".equals(upper) || "BEGIN".equals(upper)) {
            return new BeginTransactionCommand();
        }

        if ("COMMIT".equals(upper)) {
            return new CommitTransactionCommand();
        }

        if ("ROLLBACK".equals(upper)) {
            return new RollbackTransactionCommand();
        }

        throw new IllegalArgumentException("Unsupported command");
    }

    private List<String> parseColumnList(String columnsStr) {
        List<String> columns = new ArrayList<>();
        String[] parts = columnsStr.split(",");
        for (String part : parts) {
            String col = part.trim();
            if (!col.isEmpty()) {
                columns.add(col);
            }
        }
        return columns;
    }

    private List<String> parseValueList(String valuesStr) {
        List<String> values = new ArrayList<>();
        String[] parts = valuesStr.split(",");
        for (String part : parts) {
            String val = part.trim();
            // Remove quotes if present
            if (val.startsWith("'") && val.endsWith("'")) {
                val = val.substring(1, val.length() - 1);
            }
            values.add(val);
        }
        return values;
    }

    private void parseSetClause(String setClause, List<String> columns, List<String> values) {
        String[] assignments = setClause.split(",");
        for (String assignment : assignments) {
            String[] parts = assignment.split("=", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid SET clause. Use: col1=val1, col2=val2");
            }
            String col = parts[0].trim();
            String val = parts[1].trim();
            // Remove quotes if present
            if (val.startsWith("'") && val.endsWith("'")) {
                val = val.substring(1, val.length() - 1);
            }
            columns.add(col);
            values.add(val);
        }
    }

    private void validateIdentifier(String identifier) {
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be empty");
        }
        if (!identifier.matches("[A-Za-z_][A-Za-z0-9_]{0,63}")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
    }
}


