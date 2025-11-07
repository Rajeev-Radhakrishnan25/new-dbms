package com.lightweightDbms.transaction;

import java.util.List;
import java.util.Objects;

/**
 * Represents a single operation within a transaction.
 */
public final class TransactionOperation {
    private final OperationType type;
    private final String tableName;
    private final List<String> columns;
    private final List<String> values;
    private final String whereClause;
    private final long timestamp;

    /**
     * Types of operations that can be performed in a transaction.
     */
    public enum OperationType {
        INSERT,
        UPDATE,
        DELETE
    }

    /**
     * Creates a new transaction operation.
     * @param type operation type
     * @param tableName target table
     * @param columns affected columns
     * @param values new values
     * @param whereClause WHERE condition
     */
    public TransactionOperation(OperationType type, String tableName, List<String> columns, 
                              List<String> values, String whereClause) {
        this.type = Objects.requireNonNull(type, "type");
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.columns = columns;
        this.values = values;
        this.whereClause = whereClause;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @return operation type
     */
    public OperationType getType() { return type; }

    /**
     * @return table name
     */
    public String getTableName() { return tableName; }

    /**
     * @return affected columns
     */
    public List<String> getColumns() { return columns; }

    /**
     * @return new values
     */
    public List<String> getValues() { return values; }

    /**
     * @return WHERE condition
     */
    public String getWhereClause() { return whereClause; }

    /**
     * @return operation timestamp
     */
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "TransactionOperation{" +
                "type=" + type +
                ", table='" + tableName + '\'' +
                ", columns=" + columns +
                ", values=" + values +
                ", where='" + whereClause + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
