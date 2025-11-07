package com.lightweightDbms.audit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CSV-based implementation of {@link AuditLogger} backed by a file.
 */
public final class CsvAuditLogger implements AuditLogger {
    private final File file;

    /**
     * @param filePath path to CSV file; created if missing with header
     */
    public CsvAuditLogger(String filePath) {
        this.file = new File(filePath);
        ensureFile();
    }

    private void ensureFile() {
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                bw.write("timestampMillis,userId,ip,success,event");
                bw.newLine();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public synchronized void log(AuditRecord record) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(Long.toString(record.getTimestampMillis()));
            bw.write(',');
            bw.write(escape(record.getUserId()));
            bw.write(',');
            bw.write(escape(record.getIpAddress()));
            bw.write(',');
            bw.write(Boolean.toString(record.isSuccess()));
            bw.write(',');
            bw.write(escape(record.getEvent()));
            bw.newLine();
        } catch (IOException ignored) {}
    }

    @Override
    public synchronized List<AuditRecord> readAll() {
        List<AuditRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 5) continue;
                long ts = parseLong(parts[0]);
                String user = unescape(parts[1]);
                String ip = unescape(parts[2]);
                boolean success = Boolean.parseBoolean(parts[3]);
                String event = unescape(parts[4]);
                list.add(new AuditRecord(ts, user, ip, success, event));
            }
        } catch (IOException ignored) {}
        return list;
    }

    @Override
    public synchronized List<AuditRecord> recent(int limit) {
        List<AuditRecord> all = readAll();
        Collections.reverse(all);
        if (limit <= 0 || limit >= all.size()) return all;
        return new ArrayList<>(all.subList(0, limit));
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0L; }
    }

    private String escape(String v) {
        if (v == null) return "";
        String out = v.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\"")) {
            return '"' + out + '"';
        }
        return out;
    }

    private String unescape(String v) {
        String t = v;
        if (t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1).replace("\"\"", "\"");
        }
        return t;
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