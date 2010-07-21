/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static org.jboss.errai.bus.server.io.TypeDemarshallHelper._demarshallAll;
import static org.jboss.errai.common.client.protocols.SerializationParts.ENCODED_TYPE;
import static org.mvel2.util.ParseTools.handleStringEscapes;
import static org.mvel2.util.ParseTools.subArray;

/**
 * Decodes a JSON string or character array, and provides a proper collection of elements
 */
public class JSONDecoder {
    private final char[] json;
    private final int length;
    private int cursor;

    public static Object decode(String json) {
        return new JSONDecoder(json).parse();
    }

    public static Object decode(char[] json) {
        return new JSONDecoder(json).parse();
    }

    public static Object decode(char[] json, int length, int cursor) {
        return new JSONDecoder(json, length, cursor).parse();
    }

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
            return _parse(new Context(), null, false);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object _parse(Context ctx, Object collection, boolean map) {
        while (cursor != length) {
            switch (json[cursor]) {
                case '[':
                    cursor++;
                    ctx.addValue(_parse(new Context(), new ArrayList(), false));
                    break;

                case '{':
                    cursor++;
                    ctx.addValue(_parse(new Context(), new HashMap(), true));
                    break;

                case ']':
                case '}':
                    cursor++;
                    if (map && ctx.encodedType) {
                        ctx.encodedType = false;
                        try {
                            return _demarshallAll(ctx.record(collection));
                        }
                        catch (Exception e) {
                            throw new RuntimeException("Could not demarshall object", e);
                        }
                    } else {
                        return ctx.record(collection);
                    }

                case ',':
                    cursor++;
                    ctx.record(collection);
                    break;

                case '"':
                case '\'':
                    ctx.addValue(handleStringEscapes(subArray(json, cursor + 1,
                            cursor = (captureStringLiteral(json[cursor], json, cursor, length)))));
                    cursor++;
                    break;

                case ':':
                    cursor++;
                    continue;

                default:
                    if (isDigit(json[cursor]) || (json[cursor] == '-' && isDigit(json[cursor + 1]))) {
                        int start = cursor++;
                        boolean fp = false;
                        while (cursor != length && (isDigit(json[cursor]) || json[cursor] == '.')) {
                            if (json[cursor++] == '.') fp = true;
                        }

                        if (fp) {
                            ctx.addValue(parseDouble(new String(json, start, cursor - start)));
                        } else {
                            ctx.addValue(parseLong(new String(json, start, cursor - start)));
                        }

                        break;
                    } else if (isJavaIdentifierPart(json[cursor])) {
                        int start = cursor++;
                        while ((cursor != length) && isJavaIdentifierPart(json[cursor])) cursor++;

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

    public static int captureStringLiteral(final char type, final char[] expr, int cursor, int length) {
        while (++cursor < length && expr[cursor] != type) {
            if (expr[cursor] == '\\') cursor++;
        }

        if (cursor >= length || expr[cursor] != type) {
            throw new RuntimeException("unterminated literal");
        }

        return cursor;
    }

    private static class Context {
        Object lhs;
        Object rhs;
        boolean encodedType = false;

        private Context() {
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
                        if (!encodedType) encodedType = ENCODED_TYPE.equals(lhs);

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
                throw new RuntimeException("error building collection", e);
            }
            finally {
                lhs = rhs = null;
            }
        }
    }

}
