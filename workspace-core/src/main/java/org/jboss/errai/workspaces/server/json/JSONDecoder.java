package org.jboss.errai.workspaces.server.json;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Double.parseDouble;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class JSONDecoder {
    private char[] json;
    private int length;
    private int cursor;

    private Object lhs;
    private Object rhs;

    public JSONDecoder(String json) {
        this.length = (this.json = json.toCharArray()).length;
    }

    public JSONDecoder(char[] json) {
        this.length = (this.json = json).length;
    }

    public Object parse() {
        return ((ArrayList) _parse(new ArrayList(1))).get(0);
    }

    private Object _parse(Object collection) {

        while (cursor < length) {
            switch (json[cursor]) {
                case '[':
                    cursor++;
                    addValue(_parse(new ArrayList()));
                    break;

                case '{':
                    cursor++;
                    addValue(_parse(new HashMap()));
                    break;

                case ']':
                case '}':
                case ',':
                    cursor++;
                    record(collection);
                    break;

                case '"':
                case '\'':
                    int end = balancedCapture(json, cursor, json[cursor]);
                    addValue(new String(json, cursor + 1, end - cursor - 1));
                    cursor = end + 1;
                    break;

                default:
                    if (isDigit(json[cursor]) || (json[cursor] == '-' && isDigit(json[cursor + 1]))) {
                        int start = cursor++;
                        while (cursor < length && (isDigit(json[cursor]) || json[cursor] == '.')) cursor++;

                        addValue(parseDouble(new String(json, start, cursor - start)));

                        break;
                    }
                    else if (isLetter(json[cursor])) {
                        int start = cursor++;
                        while ((cursor < length) && isLetter(json[cursor])) cursor++;

                        addValue("true".equals(new String(json, start, cursor - start)) ? Boolean.TRUE : Boolean.FALSE);

                    }
                    cursor++;
            }
        }

        record(collection);

        return collection;
    }

    private void addValue(Object val) {
        if (lhs == null) {
            lhs = val;
        }
        else {
            rhs = val;
        }
    }

    private void record(Object collection) {
        if (lhs != null) {
            if (collection instanceof Map) {
                //noinspection unchecked
                ((Map) collection).put(lhs, rhs);
            }
            else {
                //noinspection unchecked
                ((Collection) collection).add(lhs);
            }
        }

        lhs = rhs = null;
    }

    /**
     * Balanced capture algorithm (taken from MVEL)
     *
     * @param chars -
     * @param start -
     * @param type  -
     * @return -
     */
    public static int balancedCapture(char[] chars, int start, char type) {
        int depth = 1;
        char term = type;
        switch (type) {
            case '[':
                term = ']';
                break;
            case '{':
                term = '}';
                break;
            case '(':
                term = ')';
                break;
        }

        if (type == term) {
            for (start++; start < chars.length; start++) {
                if (chars[start] == type) {
                    return start;
                }
            }
        }
        else {
            for (start++; start < chars.length; start++) {
                if (start < chars.length && chars[start] == '/') {
                    if (start + 1 == chars.length) return start;
                    if (chars[start + 1] == '/') {
                        start++;
                        while (start < chars.length && chars[start] != '\n') start++;
                    }
                    else if (chars[start + 1] == '*') {
                        start += 2;
                        while (start < chars.length) {
                            switch (chars[start]) {
                                case '*':
                                    if (start + 1 < chars.length && chars[start + 1] == '/') {
                                        break;
                                    }
                                case '\r':
                                case '\n':

                                    break;
                            }
                            start++;
                        }
                    }
                }
                if (start == chars.length) return start;
                if (chars[start] == '\'' || chars[start] == '"') {
                    start = captureStringLiteral(chars[start], chars, start, chars.length);
                }
                else if (chars[start] == type) {
                    depth++;
                }
                else if (chars[start] == term && --depth == 0) {
                    return start;
                }
            }
        }

        switch (type) {
            case '[':
                throw new RuntimeException("unbalanced braces [ ... ]");
            case '{':
                throw new RuntimeException("unbalanced braces { ... }");
            case '(':
                throw new RuntimeException("unbalanced braces ( ... )");
            default:
                throw new RuntimeException("unterminated string literal");
        }
    }

    public static int captureStringLiteral(final char type, final char[] expr, int cursor, int length) {
        while (++cursor < length && expr[cursor] != type) {
            if (expr[cursor] == '\\') cursor++;
        }

        if (cursor >= length || expr[cursor] != type) {
            throw new RuntimeException("unterminated literal");
        }

        return cursor;
    }


}
