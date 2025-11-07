package com.lightweightDbms.sql;

import com.lightweightDbms.storage.StorageConfig;
import com.lightweightDbms.storage.CsvUtil;
import com.lightweightDbms.index.DatabaseIndex;
import com.lightweightDbms.index.IndexEntry;
import com.lightweightDbms.transaction.Transaction;
import com.lightweightDbms.transaction.TransactionManager;
import com.lightweightDbms.transaction.TransactionOperation;

import java.io.*;
import java.util.*;

/**
 * Handles SQL operations: SHOW, USE, CREATE, DESCRIBE, SELECT, INSERT, DELETE, UPDATE.
 * Each operation type has a dedicated method for execution.
 * Maintains consistency between file storage and in-memory index.
 */
public final class Query {
    private final StorageConfig storage;
    private final DatabaseIndex index;
    private TransactionManager transactionManager;
    private String currentDatabase;

    /**
     * @param storage storage configuration
     */
    public Query(StorageConfig storage) {
        this.storage = storage;
        this.index = new DatabaseIndex();
        this.transactionManager = new TransactionManager();
    }

    /**
     * Sets the transaction manager.
     * @param transactionManager transaction manager instance
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Sets the current database context.
     * @param databaseName database name
     */
    public void setCurrentDatabase(String databaseName) {
        this.currentDatabase = databaseName;
    }

    /**
     * @return current database name or null
     */
    public String getCurrentDatabase() {
        return currentDatabase;
    }

    /**
     * Shows all tables in the current database.
     * @return list of table names
     */
    public List<String> showTables() {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File dbDir = storage.databaseDir(currentDatabase);
        if (!dbDir.exists()) return new ArrayList<>();
        File[] files = dbDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files == null) return new ArrayList<>();
        List<String> tables = new ArrayList<>();
        for (File f : files) {
            String name = f.getName();
            tables.add(name.substring(0, name.length() - 4)); // remove .csv
        }
        return tables;
    }

    /**
     * Creates a new table with specified columns.
     * @param tableName table name
     * @param columns column definitions (name:type pairs)
     * @return success message
     */
    public String createTable(String tableName, List<String> columns) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' already exists.");
        }
        //noinspection ResultOfMethodCallIgnored
        tableFile.getParentFile().mkdirs();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile, false))) {
            // Write header
            bw.write(String.join(",", columns));
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create table: " + e.getMessage());
        }
        return "Table '" + tableName + "' created successfully.";
    }

    /**
     * Describes table structure (column names and types).
     * @param tableName table name
     * @return column information
     */
    public String describeTable(String tableName) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile))) {
            String header = br.readLine();
            if (header == null) return "Empty table.";
            String[] columns = header.split(",");
            StringBuilder result = new StringBuilder("Table: ").append(tableName).append("\n");
            for (int i = 0; i < columns.length; i++) {
                result.append("Column ").append(i + 1).append(": ").append(columns[i]).append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to describe table: " + e.getMessage());
        }
    }

    /**
     * Selects data from a table with optional WHERE clause.
     * @param tableName table name
     * @param columns columns to select (* for all)
     * @param whereClause optional WHERE condition
     * @return formatted result
     */
    public String selectData(String tableName, List<String> columns, String whereClause) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile))) {
            String header = br.readLine();
            if (header == null) return "Empty table.";
            String[] allColumns = header.split(",");
            List<String> selectedColumns = columns.contains("*") ? Arrays.asList(allColumns) : columns;
            StringBuilder result = new StringBuilder();
            // Header
            result.append(String.join(" | ", selectedColumns)).append("\n");
            result.append("-".repeat(selectedColumns.size() * 10)).append("\n");
            // Data rows
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseCsvLine(line);
                List<String> selectedValues = new ArrayList<>();
                for (String col : selectedColumns) {
                    int index = Arrays.asList(allColumns).indexOf(col);
                    if (index >= 0 && index < values.length) {
                        selectedValues.add(values[index]);
                    } else {
                        selectedValues.add("NULL");
                    }
                }
                result.append(String.join(" | ", selectedValues)).append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to select data: " + e.getMessage());
        }
    }

    /**
     * Inserts data into a table.
     * @param tableName table name
     * @param values values to insert
     * @return success message
     */
    public String insertData(String tableName, List<String> values) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        
        // Check if we're in a transaction
        Transaction currentTxn = transactionManager.getCurrentTransaction();
        if (currentTxn != null && currentTxn.isActive()) {
            // Add operation to transaction (temporary storage)
            TransactionOperation operation = new TransactionOperation(
                TransactionOperation.OperationType.INSERT, 
                tableName, 
                null, 
                values, 
                null
            );
            currentTxn.addOperation(operation);
            return "1 row queued for insert into '" + tableName + "' (transaction: " + currentTxn.getTransactionId() + ").";
        }
        
        // No transaction - apply immediately
        return insertDataImmediate(tableName, values);
    }

    /**
     * Immediately inserts data into a table (bypasses transaction).
     * @param tableName table name
     * @param values values to insert
     * @return success message
     */
    private String insertDataImmediate(String tableName, List<String> values) {
        File tableFile = storage.tableFile(currentDatabase, tableName);
        
        // Count existing rows to determine row number
        long rowNumber = countRows(tableFile);
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile, true))) {
            bw.write(String.join(",", values));
            bw.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Failed to insert data: " + e.getMessage());
        }
        
        // Update index with new entry (using first value as key)
        String key = values.isEmpty() ? "" : values.get(0);
        IndexEntry entry = new IndexEntry(tableName, key, rowNumber, tableFile.getPath());
        index.insert(tableName, key, entry);
        
        return "1 row inserted into '" + tableName + "'.";
    }

    /**
     * Deletes data from a table.
     * @param tableName table name
     * @param whereClause WHERE condition for deletion
     * @return success message
     */
    public String deleteData(String tableName, String whereClause) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        
        // Check if we're in a transaction
        Transaction currentTxn = transactionManager.getCurrentTransaction();
        if (currentTxn != null && currentTxn.isActive()) {
            // Add operation to transaction (temporary storage)
            TransactionOperation operation = new TransactionOperation(
                TransactionOperation.OperationType.DELETE, 
                tableName, 
                null, 
                null, 
                whereClause
            );
            currentTxn.addOperation(operation);
            return "Delete operation queued for '" + tableName + "' (transaction: " + currentTxn.getTransactionId() + ").";
        }
        
        // No transaction - apply immediately
        return deleteDataImmediate(tableName, whereClause);
    }

    /**
     * Immediately deletes data from a table (bypasses transaction).
     * @param tableName table name
     * @param whereClause WHERE condition for deletion
     * @return success message
     */
    private String deleteDataImmediate(String tableName, String whereClause) {
        File tableFile = storage.tableFile(currentDatabase, tableName);
        
        // Read all lines
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read table: " + e.getMessage());
        }
        
        if (lines.isEmpty()) {
            return "No rows to delete.";
        }
        
        String header = lines.get(0);
        List<String> rowsToDelete = new ArrayList<>();
        
        // Simple WHERE clause matching (exact match on first column)
        if (whereClause != null && !whereClause.isEmpty()) {
            String[] parts = whereClause.split("=", 2);
            if (parts.length == 2) {
                String column = parts[0].trim();
                String value = parts[1].trim().replace("'", "");
                
                // Find matching rows
                for (int i = 1; i < lines.size(); i++) {
                    String[] values = parseCsvLine(lines.get(i));
                    if (values.length > 0 && values[0].equals(value)) {
                        rowsToDelete.add(lines.get(i));
                    }
                }
            }
        } else {
            // Delete all rows (keep header)
            for (int i = 1; i < lines.size(); i++) {
                rowsToDelete.add(lines.get(i));
            }
        }
        
        // Remove rows from lines list
        lines.removeAll(rowsToDelete);
        
        // Write back to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update table: " + e.getMessage());
        }
        
        // Update index - remove deleted entries
        for (String deletedRow : rowsToDelete) {
            String[] values = parseCsvLine(deletedRow);
            if (values.length > 0) {
                index.delete(tableName, values[0]);
            }
        }
        
        return rowsToDelete.size() + " row(s) deleted from '" + tableName + "'.";
    }

    /**
     * Updates data in a table.
     * @param tableName table name
     * @param columns columns to update
     * @param values new values
     * @param whereClause WHERE condition for update
     * @return success message
     */
    public String updateData(String tableName, List<String> columns, List<String> values, String whereClause) {
        if (currentDatabase == null) {
            throw new IllegalStateException("No database selected. Use USE <database>; first.");
        }
        File tableFile = storage.tableFile(currentDatabase, tableName);
        if (!tableFile.exists()) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist.");
        }
        
        // Check if we're in a transaction
        Transaction currentTxn = transactionManager.getCurrentTransaction();
        if (currentTxn != null && currentTxn.isActive()) {
            // Add operation to transaction (temporary storage)
            TransactionOperation operation = new TransactionOperation(
                TransactionOperation.OperationType.UPDATE, 
                tableName, 
                columns, 
                values, 
                whereClause
            );
            currentTxn.addOperation(operation);
            return "Update operation queued for '" + tableName + "' (transaction: " + currentTxn.getTransactionId() + ").";
        }
        
        // No transaction - apply immediately
        return updateDataImmediate(tableName, columns, values, whereClause);
    }

    /**
     * Immediately updates data in a table (bypasses transaction).
     * @param tableName table name
     * @param columns columns to update
     * @param values new values
     * @param whereClause WHERE condition for update
     * @return success message
     */
    private String updateDataImmediate(String tableName, List<String> columns, List<String> values, String whereClause) {
        File tableFile = storage.tableFile(currentDatabase, tableName);
        
        // Read all lines
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read table: " + e.getMessage());
        }
        
        if (lines.isEmpty()) {
            return "No rows to update.";
        }
        
        String header = lines.get(0);
        String[] headerColumns = header.split(",");
        int updatedCount = 0;
        
        // Update matching rows
        for (int i = 1; i < lines.size(); i++) {
            String[] rowValues = parseCsvLine(lines.get(i));
            boolean shouldUpdate = false;
            
            // Check WHERE condition
            if (whereClause != null && !whereClause.isEmpty()) {
                String[] parts = whereClause.split("=", 2);
                if (parts.length == 2) {
                    String column = parts[0].trim();
                    String value = parts[1].trim().replace("'", "");
                    
                    // Find column index
                    int colIndex = -1;
                    for (int j = 0; j < headerColumns.length; j++) {
                        if (headerColumns[j].trim().equals(column)) {
                            colIndex = j;
                            break;
                        }
                    }
                    
                    if (colIndex >= 0 && colIndex < rowValues.length && rowValues[colIndex].equals(value)) {
                        shouldUpdate = true;
                    }
                }
            } else {
                shouldUpdate = true; // Update all rows if no WHERE clause
            }
            
            if (shouldUpdate) {
                // Update specified columns
                for (int j = 0; j < columns.size() && j < values.size(); j++) {
                    String colName = columns.get(j);
                    String newValue = values.get(j);
                    
                    // Find column index
                    for (int k = 0; k < headerColumns.length; k++) {
                        if (headerColumns[k].trim().equals(colName)) {
                            if (k < rowValues.length) {
                                rowValues[k] = newValue;
                            }
                            break;
                        }
                    }
                }
                
                // Rebuild the line
                lines.set(i, String.join(",", rowValues));
                updatedCount++;
                
                // Update index
                if (rowValues.length > 0) {
                    String oldKey = rowValues[0]; // Assuming first column is key
                    index.delete(tableName, oldKey);
                    IndexEntry newEntry = new IndexEntry(tableName, oldKey, i - 1, tableFile.getPath());
                    index.insert(tableName, oldKey, newEntry);
                }
            }
        }
        
        // Write back to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tableFile, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update table: " + e.getMessage());
        }
        
        return updatedCount + " row(s) updated in '" + tableName + "'.";
    }

    /**
     * Applies all operations in a transaction to persistent storage.
     * @param transaction transaction to apply
     * @return true if all operations applied successfully
     */
    public boolean applyTransaction(Transaction transaction) {
        try {
            for (TransactionOperation operation : transaction.getOperations()) {
                switch (operation.getType()) {
                    case INSERT:
                        insertDataImmediate(operation.getTableName(), operation.getValues());
                        break;
                    case UPDATE:
                        updateDataImmediate(operation.getTableName(), operation.getColumns(), 
                                          operation.getValues(), operation.getWhereClause());
                        break;
                    case DELETE:
                        deleteDataImmediate(operation.getTableName(), operation.getWhereClause());
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            // If any operation fails, the entire transaction should be rolled back
            return false;
        }
    }

    private long countRows(File tableFile) {
        long count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(tableFile))) {
            while (br.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            // Return 0 if can't read
        }
        return count;
    }

    private String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    cols.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') {
                    inQuotes = true;
                } else {
                    cur.append(c);
                }
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }
}
