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

package org.jboss.errai.codegen.framework.tests.literals;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.literal.LiteralFactory;
import org.jboss.errai.codegen.framework.tests.AbstractCodegenTest;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralTest extends AbstractCodegenTest {

  @Test
  public void testIntegerLiteral() {
    assertEquals("1234", LiteralFactory.getLiteral(1234).generate(Context.create()));
  }

  @Test
  public void testShortLiteral() {
    assertEquals("1234", LiteralFactory.getLiteral((short) 1234).generate(Context.create()));
  }

  @Test
  public void testLongLiteral() {
    assertEquals("1234L", LiteralFactory.getLiteral(1234L).generate(Context.create()));
  }

  @Test
  public void testDoubleLiteral() {
    assertEquals("1234.567d", LiteralFactory.getLiteral(1234.567d).generate(Context.create()));
  }

  @Test
  public void testFloatLiteral() {
    assertEquals("1234.567f", LiteralFactory.getLiteral(1234.567f).generate(Context.create()));
  }

  @Test
  public void testByteLiteral() {
    assertEquals("72", LiteralFactory.getLiteral((byte) 72).generate(Context.create()));
  }

  @Test
  public void testBooleanLiteral() {
    assertEquals("false", LiteralFactory.getLiteral(false).generate(Context.create()));
  }

  @Test
  public void testStringLiteral() {
    final String expected = "\"The quick brown fox said \\\"how do you do?\\\"\\nNew line.\\rCarriage Return!"
        + "\\t and a tab\"";

    final String input = "The quick brown fox said \"how do you do?\"\nNew line.\rCarriage Return!"
        + "\t and a tab";

    assertEquals(expected, LiteralFactory.getLiteral(input).generate(Context.create()));
  }

  @Test
  public void testStringArrayCreation() {
    final String[][] input = new String[][]{{"Hello1", "Hello2"}, {"Hello3", "Hello4"}};
    final String expected = "new String[][] { { \"Hello1\", \"Hello2\" }, { \"Hello3\", \"Hello4\" } }";

    assertEquals(expected, LiteralFactory.getLiteral(input).generate(Context.create()));
  }

  @Test
  public void testClassLiteral() {
    assertEquals("String.class", LiteralFactory.getLiteral(String.class).generate(Context.create()));
  }

  @Test
  public void testSetEncoding() {
    Set<String> s = new LinkedHashSet<String>();
    s.add("foo");
    s.add("bar");

    assertEquals("new java.util.HashSet() {\n" +
            "  {\n" +
            "    add(\"foo\");\n" +
            "    add(\"bar\");\n" +
            "  }\n" +
            "}", Stmt.load(s).generate(Context.create()));

  }

  @Test
  public void testListEncoding() {
    List<String> s = new ArrayList<String>();
    s.add("foo");
    s.add("bar");

    assertEquals("new java.util.ArrayList() {\n" +
            "  {\n" +
            "    add(\"foo\");\n" +
            "    add(\"bar\");\n" +
            "  }\n" +
            "}", Stmt.load(s).generate(Context.create()));

  }

  @Test
  public void testMapEncoding() {
    Map<String, String> s = new LinkedHashMap<String, String>();
    s.put("foo", "fooz");
    s.put("bar", "barz");

    assertEquals("new java.util.HashMap() {\n" +
            "  {\n" +
            "    put(\"foo\", \"fooz\");\n" +
            "    put(\"bar\", \"barz\");\n" +
            "  }\n" +
            "}", Stmt.load(s).generate(Context.create()));

  }
}