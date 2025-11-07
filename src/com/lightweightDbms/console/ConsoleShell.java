package com.lightweightDbms.console;

import com.lightweightDbms.audit.AuditLogger;
import com.lightweightDbms.audit.AuditRecord;
import com.lightweightDbms.db.DatabaseEngine;
import com.lightweightDbms.sql.Command;
import com.lightweightDbms.sql.SqlParser;

import java.time.Instant;
import java.util.List;
import java.util.Scanner;

/**
 * Console SQL-like shell that accepts and executes commands against a database engine.
 *
 */
public final class ConsoleShell {
    private final DatabaseEngine databaseEngine;
    private final SqlParser sqlParser;
    private final Scanner scanner;
    private final AuditLogger auditLogger;
    private final boolean isAdmin;

    /**
     * Creates a new shell instance.
     *
     * @param databaseEngine target engine for command execution
     * @param sqlParser parser to translate raw input into commands
     * @param scanner input scanner (owned by caller)
     * @param auditLogger logger to view audit records (nullable)
     * @param isAdmin whether current user has admin privileges
     */
    public ConsoleShell(DatabaseEngine databaseEngine, SqlParser sqlParser, Scanner scanner, AuditLogger auditLogger, boolean isAdmin) {
        this.databaseEngine = databaseEngine;
        this.sqlParser = sqlParser;
        this.scanner = scanner;
        this.auditLogger = auditLogger;
        this.isAdmin = isAdmin;
    }

    /**
     * Starts the interactive session until user issues EXIT/QUIT command.
     */
    public void startSession() {
        System.out.println("\nEnter SQL commands\n NOTE:\n    Use Caps recommended\n    End with ';'\n    Type EXIT; to leave.\n    Transaction commands: BEGIN TRANSACTION;, COMMIT;, ROLLBACK;");
        if (isAdmin) {
            System.out.println("Admin tip: use 'SHOW LOGS;' or 'SHOW LOGS LIMIT <N>;' to view audit records.");
        }
        while (true) {
            String promptDb = databaseEngine.getDatabaseName() == null ? "(no-db)" : databaseEngine.getDatabaseName();
            System.out.print("dbms " + promptDb + "> ");
            if (!scanner.hasNextLine()) {
                System.out.println("\nSession terminated.");
                return;
            }
            String line = scanner.nextLine();
            if (line == null) {
                return;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                String upper = line.toUpperCase();
                if (isAdmin && (upper.equals("SHOW LOGS;") || upper.startsWith("SHOW LOGS LIMIT "))) {
                    int limit = 50;
                    if (upper.startsWith("SHOW LOGS LIMIT ")) {
                        String numStr = upper.substring("SHOW LOGS LIMIT ".length());
                        if (numStr.endsWith(";")) numStr = numStr.substring(0, numStr.length() - 1);
                        try { limit = Integer.parseInt(numStr.trim()); } catch (NumberFormatException ignored) {}
                    }
                    showLogs(limit);
                    continue;
                }

                Command cmd = sqlParser.parse(line);
                if (cmd == null) {
                    System.out.println("Bye.\nClosing Shel....");
                    return;
                }
                String result = databaseEngine.execute(cmd);
                System.out.println(result);
            } catch (IllegalArgumentException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("ERROR: Unexpected failure: " + ex.getMessage());
            }
        }
    }

    private void showLogs(int limit) {
        if (auditLogger == null) {
            System.out.println("No audit logger configured.");
            return;
        }
        List<AuditRecord> records = auditLogger.recent(limit);
        if (records.isEmpty()) {
            System.out.println("No audit records.");
            return;
        }
        System.out.println("timestamp | userId | ip | success | event");
        for (AuditRecord r : records) {
            System.out.println(Instant.ofEpochMilli(r.getTimestampMillis()) + " | " + r.getUserId() + " | " + r.getIpAddress() + " | " + r.isSuccess() + " | " + r.getEvent());
        }
    }
}
