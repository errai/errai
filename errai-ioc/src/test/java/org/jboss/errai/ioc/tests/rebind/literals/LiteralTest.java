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

package org.jboss.errai.ioc.tests.rebind.literals;

import java.util.Set;

import javax.enterprise.util.TypeLiteral;

import junit.framework.TestCase;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LiteralTest extends TestCase {

  public void testIntegerLiteral() {
    assertEquals("1234", LiteralFactory.getLiteral(1234).generate(Context.create()));
  }

  public void testShortLiteral() {
    assertEquals("1234", LiteralFactory.getLiteral((short) 1234).generate(Context.create()));
  }

  public void testLongLiteral() {
    assertEquals("1234L", LiteralFactory.getLiteral(1234L).generate(Context.create()));
  }

  public void testDoubleLiteral() {
    assertEquals("1234.567d", LiteralFactory.getLiteral(1234.567d).generate(Context.create()));
  }

  public void testFloatLiteral() {
    assertEquals("1234.567f", LiteralFactory.getLiteral(1234.567f).generate(Context.create()));
  }

  public void testByteLiteral() {
    assertEquals("72", LiteralFactory.getLiteral((byte) 72).generate(Context.create()));
  }

  public void testBooleanLiteral() {
    assertEquals("false", LiteralFactory.getLiteral(false).generate(Context.create()));
  }

  public void testStringLiteral() {
    final String expected = "\"The quick brown fox said \\\"how do you do?\\\"\\nNew line.\\rCarriage Return!"
        + "\\t and a tab\"";

    final String input = "The quick brown fox said \"how do you do?\"\nNew line.\rCarriage Return!"
        + "\t and a tab";

    assertEquals(expected, LiteralFactory.getLiteral(input).generate(Context.create()));
  }

  public void testStringArrayCreation() {
    final String[][] input = new String[][]{{"Hello1", "Hello2"}, {"Hello3", "Hello4"}};
    final String expected = "new String[][] {{\"Hello1\", \"Hello2\"}, {\"Hello3\", \"Hello4\"}}";

    assertEquals(expected, LiteralFactory.getLiteral(input).generate(Context.create()));
  }

  public void testClassLiteral() {
    assertEquals("String.class", LiteralFactory.getLiteral(String.class).generate(Context.create()));
  }
}