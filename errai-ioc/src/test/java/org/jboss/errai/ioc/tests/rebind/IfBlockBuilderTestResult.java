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

package org.jboss.errai.ioc.tests.rebind;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilderTestResult {

  public static final String EMPTY_IF_BLOCK_RESULT_NO_RHS =
      "if (str.endsWith(\"abc\")) { }\n";

  public static final String EMPTY_IF_BLOCK_RESULT_LITERAL_RHS =
      "if (n == 1) { }\n";

  public static final String EMPTY_IF_BLOCK_RESULT_INSTANCE_OF_RHS =
      "if (str instanceof java.lang.String) { }\n";

  public static final String IF_ELSE_BLOCK_RESULT_NO_RHS =
      "   if (str.endsWith(\"abc\")) { " +
          " java.lang.Integer n = 0;\n" +
          "} else {" +
          " \njava.lang.Integer n = 1;\n" +
          "}\n";

  public static final String IF_ELSE_BLOCK_RESULT_RHS =
      "   if (n > m) { " +
          " java.lang.Integer n = 0;\n" +
          "} else {" +
          " \njava.lang.Integer n = 1;\n" +
          "}\n";

  public static final String IF_ELSEIF_BLOCK_RESULT_NO_RHS_NESTED =
      "   if (s.endsWith(\"abc\")) {\n" +
          " n = 0;\n" +
          "} else {\n" +
          " if (s.startsWith(\"def\")) { " +
          "   n = 1;\n" +
          " };\n" +
          "}\n";

  public static final String IF_ELSEIF_BLOCK_RESULT_NO_RHS =
      "   if (s.endsWith(\"abc\")) {\n" +
          " n = 0;\n" +
          "} else if (s.startsWith(\"def\")) { " +
          " n = 1;\n" +
          "}\n";

  public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS_NESTED =
      "   if (s.endsWith(\"abc\")) {\n" +
          " n = 0;\n" +
          "} else {\n" +
          " if (s.startsWith(\"def\")) { " +
          "   n = 1;\n" +
          " } else { " +
          "   n = 2;\n" +
          " }\n;\n" +
          "}\n";

  public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS =
      "   if (s.endsWith(\"abc\")) {\n" +
          " n = 0;\n" +
          "} else if (s.startsWith(\"def\")) { " +
          " n = 1;\n" +
          "} else { " +
          " n = 2;\n" +
          "}\n";

  public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_RHS_NESTED =
      "   if (n > m) {\n" +
          " n = 0;\n" +
          "} else {\n" +
          " if (m > n) { " +
          "   n = 1;\n" +
          " } else { " +
          "   n = 2;\n" +
          " };\n" +
          "}\n";

  public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_RHS =
      "   if (n > m) {\n" +
          " n = 0;\n" +
          "} else if (m > n) { " +
          " n = 1;\n" +
          "} else if (m == n) { " +
          " n = 2;\n" +
          "} else { " +
          " n = 3;\n" +
          "}\n";
}