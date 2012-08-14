/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.codegen.util;

import org.mvel2.CompileException;
import org.mvel2.util.ParseTools;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class QuickDeps {
  private static final Set<String> RESERVED_KEYWORDS = new HashSet<String>() {
    {
      add("abstract");
      add("assert");
      add("boolean");
      add("break");
      add("byte");
      add("case");
      add("catch");
      add("char");
      add("class");
      add("const");
      add("continue");
      add("default");
      add("do");
      add("double");
      add("else");
      add("enum");
      add("extends");
      add("final");
      add("finally");
      add("float");
      add("for");
      add("goto");
      add("if");
      add("implements");
      add("import");
      add("instanceof");
      add("int");
      add("interface");
      add("long");
      add("native");
      add("new");
      add("package");
      add("private");
      add("protected");
      add("public");
      add("return");
      add("short");
      add("static");
      add("strictfp");
      add("super");
      add("switch");
      add("synchronized");
      add("this");
      add("throw");
      add("throws");
      add("transient");
      add("try");
      add("void");
      add("volatile");
      add("while");

      add("false");
      add("null");
      add("true");
    }
  };

  public static Set<String> getQuickTypeDependencyList(final String javaSource, ClassLoader classLoader) {
    if (classLoader == null) {
      classLoader = Thread.currentThread().getContextClassLoader();
    }

    final IdentifierTokenizer identifierTokenizer = new IdentifierTokenizer(javaSource);

    final Map<String, String> imports = new HashMap<String, String>(32);
    final Set<String> wildcardPackages = new HashSet<String>(32);
    wildcardPackages.add("java.lang");

    final Set<String> usedTypes = new HashSet<String>(100);

    String token;
    while ((token = identifierTokenizer.nextToken()) != null) {
      if (RESERVED_KEYWORDS.contains(token)) {
        if ("import".equals(token)) {
          token = identifierTokenizer.nextToken();

          if (token.endsWith("*")) {
            wildcardPackages.add(token.substring(0, token.lastIndexOf('.')));
          }
          else {
            imports.put(token, token);
            imports.put(token.substring(token.lastIndexOf('.') + 1), token);
          }
        }
        else if ("package".equals(token)) {
          wildcardPackages.add(identifierTokenizer.nextToken());
        }
        continue;
      }

      if (token.endsWith(".class")) {
        token = token.substring(0, token.length() - 6);
      }
      /**
       * This handles the case where there's whitespace or a comment after the union operator.
       */
      else if (token.endsWith(".")) {
        token = token.substring(0, token.length() - 1);
      }

      if (imports.containsKey(token)) {
        usedTypes.add(imports.get(token));
      }
      else {
        wildcardMatch(classLoader, imports, wildcardPackages, usedTypes, token);
      }
    }

    return usedTypes;
  }

  private static void wildcardMatch(final ClassLoader classLoader,
                                    final Map<String, String> imports,
                                    final Set<String> wildcardPackages,
                                    final Set<String> usedTypes,
                                    final String name) {


    for (final String pkg : wildcardPackages) {

      final String fqcn = pkg + "." + name;
      final String slashified = fqcn.replaceAll("\\.", "/");
      final String source = slashified + ".java";
      final String clazz = slashified + ".class";

      URL url;
      if ((url = classLoader.getResource(source)) != null
          || (url = classLoader.getResource(clazz)) != null) {

        final String urlFile = url.getFile();
        if (urlFile.endsWith(source) || urlFile.endsWith(clazz)) {
          imports.put(fqcn, fqcn);
          imports.put(name, fqcn);
          usedTypes.add(fqcn);
          return;
        }
      }
    }
  }

  private static class IdentifierTokenizer {
    private final String expr;
    private int i;
    private int startToken;
    private boolean tokenCapture;

    private IdentifierTokenizer(final String expr) {
      this.expr = expr;
    }

    public String nextToken() {
      char c;
      for (; i < expr.length(); i++) {
        switch (c = expr.charAt(i)) {
          case '"':
          case '\'':
            if (i < expr.length()) {
              i = captureStringLiteral(c, expr, i, expr.length()) + 1;
            }
            break;

          case '/':
            if (tokenCapture) {
              tokenCapture = false;
              return expr.substring(startToken, i).trim();
            }

            if (i < expr.length() && expr.charAt(i + 1) == '*') {
              i += 2;
              while (i < expr.length() && !(c == '*' && expr.charAt(i + 1) == '/')) {
                c = expr.charAt(++i);
              }
              i++;
            }
            else if (expr.charAt(i + 1) == '/') {
              while (i < expr.length() && c != '\n') {
                c = expr.charAt(++i);
              }
            }
            break;

          default:
            if (ParseTools.isIdentifierPart(c)
                || c == '.'
                || (c == '*' && expr.charAt(i - 1) == '.')) {

              if (!tokenCapture) {
                startToken = i;
                tokenCapture = true;
              }
            }
            else if (tokenCapture) {
              tokenCapture = false;
              return expr.substring(startToken, i);
            }
        }
      }

      return null;
    }
  }

  public static int captureStringLiteral(final char type, final String expr, int cursor, final int end) {
    while (++cursor < end && expr.charAt(cursor) != type) {
      if (expr.charAt(cursor) == '\\') cursor++;
    }

    if (cursor >= end || expr.charAt(cursor) != type) {
      throw new RuntimeException("unterminated string literal");
    }

    return cursor;
  }
}
