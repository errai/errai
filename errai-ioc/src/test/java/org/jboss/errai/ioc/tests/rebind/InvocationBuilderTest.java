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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Refs;
import org.junit.Test;

/**
 * Tests the generation of method invocations using the {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testSingleInvocation() {
    String s = StatementBuilder.create()
        .addVariable("obj", Object.class)
        .loadVariable("obj")
        .invoke("toString")
        .toJavaString();

    assertEquals("failed to generate invocation on variable", "obj.toString()", s);
  }

  @Test
  public void testChainedInvocations() {
    String s = StatementBuilder.create()
        .addVariable("i", Integer.class)
        .addVariable("regex", String.class)
        .addVariable("replacement", String.class)
        .loadVariable("i")
        .invoke("toString")
        .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"))
        .toJavaString();

    assertEquals("failed to generate chained invocations on variable",
        "i.toString().replaceAll(regex, replacement)", s);
  }

  @Test
  public void testInvokeWithLiteralParameters() {
    String result = StatementBuilder.create().addVariable("s", String.class)
        .loadVariable("s").invoke("replaceAll", "foo", "foo\t\n").toJavaString();

    assertEquals("failed to generate invocation using literal parameters",
        "s.replaceAll(\"foo\", \"foo\\t\\n\")", result);
  }

  @Test
  public void testInvokeOnLiteral() {
    String result = StatementBuilder.create().loadLiteral("foo").invoke("toString").toJavaString();

    assertEquals("failed to generate invocation using literal parameters",
        "\"foo\".toString()", result);
  }

  @Test
  public void testInvokeOnBestMatchingMethod() {
    String s = StatementBuilder.create()
        .addVariable("n", Integer.class)
        .loadVariable("n")
        // 1 will be inferred to LiteralValue<Integer>, equals(Integer.class)
        // should be matched equals(Object.class)
        .invoke("equals", 1)
        .toJavaString();

    assertEquals("failed to generate invocation on matched method", "n.equals(1)", s);
  }

  @Test
  public void testInvokeUndefinedMethodOnVariable() {
    try {
      StatementBuilder.create()
          .addVariable("obj", Object.class)
          .addVariable("param", String.class)
          .loadVariable("obj")
          .invoke("undefinedMethod", Variable.get("param"))
          .toJavaString();
      fail("expected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      // expected
      assertEquals("Wrong exception thrown", udme.getMethodName(), "undefinedMethod");
    }
  }

  @Test
  public void testInvokeChainedUndefinedMethod() {
    try {
      StatementBuilder.create()
          .addVariable("s", String.class)
          .addVariable("regex", String.class)
          .addVariable("replacement", String.class)
          .loadVariable("s")
          .invoke("replaceAll", Variable.get("regex"), Variable.get("replacement"))
          .invoke("undefinedMethod", Variable.get("regex"), Variable.get("replacement"))
          .toJavaString();
      fail("expected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      // expected
      assertEquals("Wrong exception thrown", udme.getMethodName(), "undefinedMethod");
    }
  }

  @Test
  public void testInvokeOnUndefinedVariable() {
    try {
      // injector undefined
      StatementBuilder.create()
          .loadVariable("injector")
          .invoke("provide", Refs.get("param"), Refs.get("param2"))
          .toJavaString();
      fail("expected OutOfScopeException");
    }
    catch (OutOfScopeException oose) {
      // expected
      assertTrue("Wrong exception thrown", oose.getMessage().contains("injector"));
    }
  }

  @Test
  public void testInvokeWithUndefinedVariable() {
    try {
      // param2 undefined
      StatementBuilder.create()
          .addVariable("obj", Object.class)
          .addVariable("param", String.class)
          .loadVariable("obj")
          .invoke("undefinedMethod", Variable.get("param"), Variable.get("param2"))
          .toJavaString();
      fail("expected OutOfScopeException");
    }
    catch (OutOfScopeException oose) {
      // expected
      assertTrue(oose.getMessage().contains("param2"));
    }
  }

  @Test
  public void testInvokeUsingStandardizedLoadVariableReference() {
    Context context = ContextBuilder.create()
        .addVariable("s", String.class)
        .getContext();

    String s = StatementBuilder.create(context)
        .load(Variable.get("s"))
        .invoke("toUpperCase").toJavaString();

    assertEquals("failed using load() passing a variable reference", "s.toUpperCase()", s);
  }

  @Test
  public void testInvokeUsingStandardizedLoadVariableInstance() {
    Context context = ContextBuilder.create()
        .addVariable("s", String.class)
        .getContext();

    Variable v = Variable.create("s", String.class);
    String s = StatementBuilder.create(context)
        .load(v)
        .invoke("toUpperCase").toJavaString();

    assertEquals("failed using load() passing a variable instance", "s.toUpperCase()", s);
  }

  @Test
  public void testInvokeUsingStandardizedLoadLiteral() {
    String s = StatementBuilder.create()
        .load("foo")
        .invoke("toUpperCase").toJavaString();

    assertEquals("failed injecting literal with load()", "\"foo\".toUpperCase()", s);
  }

  @Test
  public void testInvokeWithParameterTypeConversionOfIntegerToString() {
    String s = StatementBuilder.create()
        .addVariable("str", String.class)
        .loadVariable("str")
        .invoke("endsWith", 123)
        .toJavaString();

    assertEquals("failed to generate invocation with parameter type conversion", "str.endsWith(\"123\")", s);
  }

  @Test
  public void testInvokeWithParameterTypeConversionOfStringToInteger() {
    String s = StatementBuilder.create()
        .addVariable("str", String.class)
        .loadVariable("str")
        .invoke("substring", "1", "3")
        .toJavaString();

    assertEquals("failed to generate invocation with parameter type conversion", "str.substring(1, 3)", s);
  }

  @Test
  public void testInvokeWithParameterTypeConversionOfVariable() {
    Context c = Context.create().addVariable("n", Integer.class, 123);
    String s = StatementBuilder.create(c)
        .addVariable("str", String.class)
        .loadVariable("str")
        .invoke("endsWith", c.getVariable("n").getValue())
        .toJavaString();

    assertEquals("failed to generate invocation with parameter type conversion of variable",
        "str.endsWith(\"123\")", s);
  }

  @Test
  public void testInvokeStaticMethod() {
    String s = StatementBuilder.create()
        .invokeStatic(Integer.class, "getInteger", "123")
        .toJavaString();

    assertEquals("failed to generate static method invocation", "Integer.getInteger(\"123\")", s);
  }

  @Test
  public void testInvokeUndefinedStaticMethod() {
    try {
      StatementBuilder.create()
          .invokeStatic(Integer.class, "intValue")
          .toJavaString();
      fail("expected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      assertEquals(udme.getMethodName(), "intValue");
    }
  }

  @Test
  public void testInvokeUndefinedStaticMethodWithParameter() {
    try {
      StatementBuilder.create()
          .invokeStatic(Integer.class, "undefinedMethod", "123")
          .toJavaString();
      fail("expected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      assertEquals(udme.getMethodName(), "undefinedMethod");
    }
  }
}