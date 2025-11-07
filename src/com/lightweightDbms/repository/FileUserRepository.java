package com.lightweightDbms.repository;

import com.lightweightDbms.exception.UserAlreadyExistsException;
import com.lightweightDbms.model.User;
import com.lightweightDbms.storage.StorageConfig;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File-backed user repository storing records in users.csv.
 */
public final class FileUserRepository implements UserRepository {
    private final StorageConfig config;
    private final Map<String, User> cache = new ConcurrentHashMap<>();

    /**
     * @param config storage configuration
     */
    public FileUserRepository(StorageConfig config) {
        this.config = config;
        load();
    }

    private void load() {
        File file = config.usersFile();
        if (!file.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                bw.write("userId,hashedPassword,securityQuestion,securityAnswer,createdAt,admin,lastSuccessfulLoginAt");
                bw.newLine();
            } catch (IOException ignored) {}
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = parseCsv(line);
                if (p.length < 7) continue;
                String userId = unescape(p[0]);
                String hashedPassword = unescape(p[1]);
                String q = unescape(p[2]);
                String a = unescape(p[3]);
                long created = parseLong(p[4]);
                boolean admin = Boolean.parseBoolean(p[5]);
                long last = parseLong(p[6]);
                User u = new User(userId, hashedPassword, q, a);
                // Restore fields
                try {
                    java.lang.reflect.Field createdAt = User.class.getDeclaredField("createdAt");
                    createdAt.setAccessible(true);
                    createdAt.set(u, created);
                } catch (Exception ignored) {}
                u.setAdmin(admin);
                u.setLastSuccessfulLoginAt(last);
                cache.put(userId, u);
            }
        } catch (IOException ignored) {}
    }

    private void persistAll() {
        File file = config.usersFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write("userId,hashedPassword,securityQuestion,securityAnswer,createdAt,admin,lastSuccessfulLoginAt");
            bw.newLine();
            for (User u : cache.values()) {
                bw.write(escape(u.getUserId()));
                bw.write(',');
                bw.write(escape(u.getHashedPassword()));
                bw.write(',');
                bw.write(escape(u.getSecurityQuestion()));
                bw.write(',');
                bw.write(escape(u.getSecurityAnswer()));
                bw.write(',');
                bw.write(Long.toString(u.getCreatedAt()));
                bw.write(',');
                bw.write(Boolean.toString(u.isAdmin()));
                bw.write(',');
                bw.write(Long.toString(u.getLastSuccessfulLoginAt()));
                bw.newLine();
            }
        } catch (IOException ignored) {}
    }

    @Override
    public void addUser(User user) throws UserAlreadyExistsException {
        if (cache.containsKey(user.getUserId())) {
            throw new UserAlreadyExistsException("User with ID '" + user.getUserId() + "' already exists");
        }
        cache.put(user.getUserId(), user);
        persistAll();
    }

    @Override
    public User getUserById(String userId) {
        return cache.get(userId);
    }

    @Override
    public boolean userExists(String userId) {
        return cache.containsKey(userId);
    }

    @Override
    public int getUserCount() {
        return cache.size();
    }

    @Override
    public void updateUser(User user) {
        if (user == null) return;
        cache.put(user.getUserId(), user);
        persistAll();
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

    private String[] parseCsv(String line) {
        java.util.List<String> cols = new java.util.ArrayList<>();
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

    private long parseLong(String s) { try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0L; } }
}


