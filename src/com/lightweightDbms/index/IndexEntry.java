package com.lightweightDbms.index;

import java.util.Objects;

/**
 * Represents an entry in the database index, storing metadata for efficient retrieval.
 */
public final class IndexEntry {
    private final String tableName;
    private final String key;
    private final long rowNumber;
    private final String filePath;

    /**
     * @param tableName name of the table
     * @param key indexed key value
     * @param rowNumber row number in the table file
     * @param filePath path to the table file
     */
    public IndexEntry(String tableName, String key, long rowNumber, String filePath) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.key = Objects.requireNonNull(key, "key");
        this.rowNumber = rowNumber;
        this.filePath = Objects.requireNonNull(filePath, "filePath");
    }

    /**
     * @return table name
     */
    public String getTableName() { return tableName; }

    /**
     * @return indexed key value
     */
    public String getKey() { return key; }

    /**
     * @return row number in the table file
     */
    public long getRowNumber() { return rowNumber; }

    /**
     * @return file path to the table
     */
    public String getFilePath() { return filePath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexEntry that = (IndexEntry) o;
        return rowNumber == that.rowNumber &&
                Objects.equals(tableName, that.tableName) &&
                Objects.equals(key, that.key) &&
                Objects.equals(filePath, that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, key, rowNumber, filePath);
    }

    @Override
    public String toString() {
        return "IndexEntry{table='" + tableName + "', key='" + key + "', row=" + rowNumber + ", file='" + filePath + "'}";
    }
}
