/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.codegen.util;

import org.mvel2.util.ParseTools;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class PrettyPrinter {
  public static String prettyPrintJava(String input) {
    StringBuilder out = new StringBuilder();
    StringBuilder lineBuffer = new StringBuilder();

    char[] expr = input.toCharArray();

    int indentLevel = 0;
    int statementIndent = 0;

    for (int i = 0; i < expr.length; i++) {
      switch (expr[i]) {
        case '{':
          lineBuffer.append('{');
          writeToBuffer(out, lineBuffer, indentLevel++, statementIndent);
          lineBuffer = new StringBuilder();
          break;

        case '}':
          writeToBuffer(out, lineBuffer, --indentLevel, statementIndent);
          lineBuffer = new StringBuilder();
          lineBuffer.append('}');
          break;

        case '"':
        case '\'':
          int start = i;
          i = ParseTools.balancedCapture(expr, i, expr[i]);
          lineBuffer.append(new String(expr, start, i - start + 1));
          break;

        case '\n':
          writeToBuffer(out, lineBuffer, indentLevel, statementIndent);
          out.append('\n');
          lineBuffer = new StringBuilder();
          break;

        default:
          if (Character.isWhitespace(expr[i])) {
            lineBuffer.append(" ");
            i = skipWhitespace(expr, i) - 1;
          }
          else {
            lineBuffer.append(expr[i]);
          }
      }
    }

    if (lineBuffer.length() != 0) {
      writeToBuffer(out, lineBuffer, indentLevel, statementIndent);
    }

    return out.toString();
  }

  private static void writeToBuffer(StringBuilder out, StringBuilder lineBuffer, int indentLevel, int statementIndent) {
    String trimmedLineBuffer = lineBuffer.toString().trim();

    if (trimmedLineBuffer.length() == 0) {
      return;
    }

    out.append(pad((indentLevel + statementIndent) * 4)).append(trimmedLineBuffer);
  }

  private static String pad(int amount) {
    if (amount <= 0) return "";

    char[] pad = new char[amount];
    for (int i = 0; i < amount; i++) {
      pad[i] = ' ';
    }
    return new String(pad);
  }

  public static int skipWhitespace(char[] expr, int cursor) {
    Skip:
    while (cursor != expr.length) {
      switch (expr[cursor]) {
        case '\n':

        case '\r':
          cursor++;
          continue;
//        case '/':
//          if (cursor + 1 != expr.length) {
//            switch (expr[cursor + 1]) {
//              case '/':
//                cursor++;
//                while (cursor != expr.length && expr[cursor] != '\n') cursor++;
//                if (cursor != expr.length) cursor++;
//
//                continue;
//
//              case '*':
//                int len = expr.length - 1;
//                cursor++;
//                while (cursor != len && !(expr[cursor] == '*' && expr[cursor + 1] == '/')) {
//                   cursor++;
//                }
//                if (cursor != len) cursor += 2;
//                continue;
//
//              default:
//                break Skip;
//
//            }
//          }
        default:
          if (!Character.isWhitespace(expr[cursor])) break Skip;

      }
      cursor++;
    }
    return cursor;

  }
}
