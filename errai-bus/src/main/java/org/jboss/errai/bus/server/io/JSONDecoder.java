package org.jboss.errai.bus.server.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Double.parseDouble;

public class JSONDecoder {
    private char[] json;
    private int length;
    private int cursor;

    public JSONDecoder(String json) {
        this.length = (this.json = json.toCharArray()).length;
    }

    public JSONDecoder(char[] json) {
        this.length = (this.json = json).length;
    }

    public JSONDecoder(char[] json, int length, int cursor) {
        this.json = json;
        this.length = length;
        this.cursor = cursor;
    }

    public Object parse() {
        try {
            return _parse(new Context(), null);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Object _parse(Context ctx, Object collection) {
        while (cursor < length) {
            switch (json[cursor]) {
                case '[':
                    cursor++;
                    ctx.addValue(_parse(new Context(), new ArrayList()));
                    break;

                case '{':
                    cursor++;
                    ctx.addValue(_parse(new Context(), new HashMap()));
                    break;

                case ']':
                case '}':
                    ctx.exit();
                    if (!ctx.isNest()) {
                        cursor++;
                        return ctx.record(collection);
                    }
                case ',':

                    cursor++;
                    ctx.record(collection);

                    break;

                case '"':
                case '\'':
                    int end = balancedCapture(json, cursor, json[cursor]);
                    ctx.addValue(new String(json, cursor + 1, end - cursor - 1));
                    cursor = end + 1;
                    break;

                default:
                    if (isDigit(json[cursor]) || (json[cursor] == '-' && isDigit(json[cursor + 1]))) {
                        int start = cursor++;
                        while (cursor < length && (isDigit(json[cursor]) || json[cursor] == '.')) cursor++;

                        ctx.addValue(parseDouble(new String(json, start, cursor - start)));

                        break;
                    } else if (isJavaIdentifierPart(json[cursor])) {
                        int start = cursor++;
                        while ((cursor < length) && isJavaIdentifierPart(json[cursor])) cursor++;

                        String s = new String(json, start, cursor - start);
                        if ("true".equals(s) || "false".equals(s)) {
                            ctx.addValue("true".equals(s) ? Boolean.TRUE : Boolean.FALSE);
                        } else if ("null".equals(s)) {
                            ctx.addValue(null);
                        } else {
                            ctx.addValue(s);
                        }
                        continue;
                    }
                    cursor++;
            }
        }

        return ctx.record(collection);
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
        } else {
            for (start++; start < chars.length; start++) {
                if (start < chars.length && chars[start] == '/') {
                    if (start + 1 == chars.length) return start;
                    if (chars[start + 1] == '/') {
                        start++;
                        while (start < chars.length && chars[start] != '\n') start++;
                    } else if (chars[start + 1] == '*') {
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
                } else if (chars[start] == type) {
                    depth++;
                } else if (chars[start] == term && --depth == 0) {
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

    private class Context {
        Object lhs;
        Object rhs;
        private int nest;

        private Context() {
        }

        private Context(int nest) {
            this.nest = nest;
        }

        private void addValue(Object val) {
            if (lhs == null) {
                lhs = val;
            } else {
                rhs = val;
            }
        }

        private Object record(Object collection) {
            try {
                if (lhs != null) {
                    if (collection instanceof Map) {
                        //noinspection unchecked
                        ((Map) collection).put(lhs, rhs);
                    } else {
                        if (collection == null) return lhs;
                        //noinspection unchecked
                        ((Collection) collection).add(lhs);
                    }
                }
                return collection;
            }
            catch (ClassCastException e) {
                e.printStackTrace();
                return null;
            }
            finally {
                lhs = rhs = null;
            }
        }

        public void nest() {
            nest++;
        }

        public void exit() {
            nest--;
        }

        public boolean isNest() {
            return nest >= 0;
        }
    }
}
