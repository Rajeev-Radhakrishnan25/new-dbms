package com.lightweightDbms.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV helpers with support for a custom field-delimiter inside values.
 */
public final class CsvUtil {
    private CsvUtil() {}

    /**
     * Escapes a value for CSV and joins subfields with a custom delimiter.
     *
     * @param values values to join (may be single value)
     * @param delimiter delimiter to join subfields
     * @param escape escape sequence to escape delimiter
     * @return CSV-safe string
     */
    public static String joinWithDelimiter(List<String> values, char delimiter, String escape) {
        String joined = join(values, delimiter, escape);
        return escapeCsv(joined);
    }

    /**
     * Splits a CSV field content into subfields using a custom delimiter.
     *
     * @param field CSV field content (already unescaped)
     * @param delimiter delimiter used for subfields
     * @param escape escape sequence
     * @return subfields
     */
    public static List<String> splitWithDelimiter(String field, char delimiter, String escape) {
        List<String> out = new ArrayList<>();
        if (field == null || field.isEmpty()) return out;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c == delimiter) {
                out.add(cur.toString());
                cur.setLength(0);
            } else if (field.startsWith(escape, i)) {
                // Skip escape and add next char if exists
                i += escape.length();
                if (i < field.length()) cur.append(field.charAt(i));
            } else {
                cur.append(c);
            }
        }
        out.add(cur.toString());
        return out;
    }

    private static String join(List<String> values, char delimiter, String escape) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i) == null ? "" : values.get(i);
            sb.append(escapeDelimiters(v, delimiter, escape));
            if (i + 1 < values.size()) sb.append(delimiter);
        }
        return sb.toString();
    }

    private static String escapeDelimiters(String value, char delimiter, String escape) {
        String d = String.valueOf(delimiter);
        return value.replace(d, escape + d);
    }

    private static String escapeCsv(String v) {
        String out = v.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\"")) {
            return '"' + out + '"';
        }
        return out;
    }
}


