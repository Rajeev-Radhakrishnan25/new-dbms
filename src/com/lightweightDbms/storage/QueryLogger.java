package com.lightweightDbms.storage;

import java.io.*;

/**
 * Appends processed SQL-like queries with outcomes to a CSV file.
 */
public final class QueryLogger {
    private final StorageConfig config;

    /**
     * @param config storage config
     */
    public QueryLogger(StorageConfig config) {
        this.config = config;
        ensureFile();
    }

    private void ensureFile() {
        File f = config.queriesFile();
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
                bw.write("timestampMillis,userId,command,status,details");
                bw.newLine();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Writes one query record.
     * @param userId user executing
     * @param command raw command
     * @param status OK/ERROR
     * @param details message
     */
    public synchronized void log(String userId, String command, String status, String details) {
        File f = config.queriesFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
            bw.write(Long.toString(System.currentTimeMillis()));
            bw.write(',');
            bw.write(escape(userId));
            bw.write(',');
            bw.write(escape(command));
            bw.write(',');
            bw.write(escape(status));
            bw.write(',');
            bw.write(escape(details));
            bw.newLine();
        } catch (IOException ignored) {}
    }

    private String escape(String v) {
        if (v == null) return "";
        String out = v.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\"")) {
            return '"' + out + '"';
        }
        return out;
    }
}


