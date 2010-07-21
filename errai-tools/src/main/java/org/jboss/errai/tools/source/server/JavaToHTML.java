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
package org.jboss.errai.tools.source.server;

import java.util.HashSet;
import java.util.Set;


public class JavaToHTML {
    public static String format(String in) {
        StringBuilder output = new StringBuilder("<tt style='color:black;'>");

        char[] arr = in.toCharArray();
        int start = 0;
        boolean capture = false;

        for (int cursor = 0; cursor < arr.length; cursor++) {
            if (Character.isWhitespace(arr[cursor])) {
                if (capture) {
                    doCapture(arr, output, start, cursor);
                    capture = false;
                }

            } else if (arr[cursor] == '"' || arr[cursor] == '\'') {
                if (capture) {
                    output.append(getHTMLizedString(arr, start, cursor - start));
                    capture = false;
                }

                output.append(FMT_STR_LITERAL);
                cursor = balancedCapture(arr, start = cursor, arr[cursor]);
                output.append(getHTMLizedString(arr, start, ++cursor - start));
                output.append(FMT_STR_LITERAL_END);

            } else if (!capture) {
                if (Character.isJavaIdentifierPart(arr[cursor])) {
                    capture = true;
                    start = cursor;
                } else if (arr[cursor] == '/') {
                    start = cursor++;
                    if (arr[cursor] == '/') {
                        output.append(FMT_COMMENT);
                        while (cursor != arr.length && arr[cursor] != '\n') cursor++;

                        String comment = getHTMLizedString(arr, start, ++cursor - start + 1);
                        output.append(comment);
                        cursor++;
                        output.append(FMT_COMMENT_END);
                    } else if (arr[cursor] == '*') {
                        output.append(FMT_COMMENT);
                        while (cursor != arr.length && !(arr[cursor] == '*' && arr[cursor + 1] == '/')) cursor++;

                        String comment = getHTMLizedString(arr, start, ++cursor - start + 1);
                        output.append(comment);
                        cursor++;
                        output.append(FMT_COMMENT_END);
                    } else {
                        output.append(arr, start, cursor - start);
                    }
                } else if (arr[cursor] == '@') {
                    start = cursor++;
                    while (cursor != arr.length && Character.isJavaIdentifierPart(arr[cursor])) cursor++;
                    String token = new String(arr, start, cursor - start);
                    output.append(FMT_ANNOTATION);
                    output.append(token);
                    output.append(FMT_ANNOTATION_END);
                }
            }


            switch (arr[cursor]) {
                case ' ':
                    output.append("&nbsp;");
                    break;
                case '\t':
                    output.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    break;
                case '\n':
                    output.append("<br/>");
                    break;
                case '<':
                    output.append("&lt;");
                    break;
                case '>':
                    output.append("&gt;");
                    break;
                case '{':
                case '(':
                    if (capture) {
                        doCapture(arr, output, start, cursor);
                        capture = false;
                    }
                    output.append(arr[cursor]);
                    break;
                default:
                    if (!capture) output.append(arr[cursor]);
            }


        }

        return output.append("</tt>").toString()
                .replaceAll("\n", "<br/>");

    }

    private static String getHTMLizedString(char[] arr, int start, int length) {
        return new String(arr, start, length)
                .replaceAll(" ", "&nbsp;")
                .replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }


    private static void doCapture(char[] arr, StringBuilder output, int start, int cursor) {
        String tk = new String(arr, start, cursor - start).trim();
        if (LITERALS.contains(tk)) {
            output.append(FMT_KEYWORD);
            output.append(tk);
            output.append(FMT_KEYWORD_END);
        } else {
            output.append(tk);
        }
    }

    private static final String FMT_ANNOTATION = "<span style='color:darkgoldenrod;'>";
    private static final String FMT_ANNOTATION_END = "</span>";

    private static final String FMT_STR_LITERAL = "<span style='color:darkgreen'>";
    private static final String FMT_STR_LITERAL_END = "</span>";

    private static final String FMT_KEYWORD = "<strong style='color:darkblue'>";
    private static final String FMT_KEYWORD_END = "</strong>";

    private static final String FMT_COMMENT = "<span style='color:grey'>";
    private static final String FMT_COMMENT_END = "</span>";

    private static final Set<String> LITERALS = new HashSet<String>();

    static {
        LITERALS.add("public");
        LITERALS.add("private");
        LITERALS.add("protected");
        LITERALS.add("final");
        LITERALS.add("void");
        LITERALS.add("class");
        LITERALS.add("interface");
        LITERALS.add("static");
        LITERALS.add("package");
        LITERALS.add("import");
        LITERALS.add("implements");
        LITERALS.add("extends");
        LITERALS.add("try");
        LITERALS.add("catch");
        LITERALS.add("finally");
        LITERALS.add("while");
        LITERALS.add("for");
        LITERALS.add("do");
        LITERALS.add("if");
        LITERALS.add("else");
        LITERALS.add("true");
        LITERALS.add("false");
        LITERALS.add("new");
        LITERALS.add("this");
        LITERALS.add("super");
        LITERALS.add("switch");
        LITERALS.add("case");
        LITERALS.add("break");
        LITERALS.add("continue");
        LITERALS.add("return");
        LITERALS.add("throw");
        LITERALS.add("volatile");
        LITERALS.add("synchronized");
        LITERALS.add("null");

        LITERALS.add("int");
        LITERALS.add("char");
        LITERALS.add("byte");
        LITERALS.add("long");
        LITERALS.add("double");
        LITERALS.add("float");
        LITERALS.add("short");
        LITERALS.add("boolean");
    }

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

        return start;
    }

    public static int captureStringLiteral(final char type, final char[] expr, int cursor, int length) {
        while (++cursor < length && expr[cursor] != type) {
            if (expr[cursor] == '\\') cursor++;
        }

        return cursor;
    }
}
