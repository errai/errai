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

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testAddVariableWithLiteralInitialization() {
    Context ctx = Context.create();
    StatementBuilder.create().addVariable("n", Integer.class, 10).generate(ctx);

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    ctx = Context.create();
    StatementBuilder.create().addVariable("n", 10).generate(ctx);

    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    ctx = Context.create();
    StatementBuilder.create().addVariable("n", "10").generate(ctx);

    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());

    ctx = Context.create();
    StatementBuilder.create().addVariable("n", Integer.class, "10").generate(ctx);

    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    try {
      ctx = StatementBuilder.create().addVariable("n", Integer.class, "abc").getContext();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }

  @Test
  public void testAddVariableWithObjectInitialization() {
    Context ctx = Context.create();
    StatementBuilder.create().addVariable("str", String.class,
        ObjectBuilder.newInstanceOf(String.class)).generate(ctx);

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());

    ctx = Context.create();
    StatementBuilder.create().addVariable("str", ObjectBuilder.newInstanceOf(String.class)).generate(ctx);

    str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }

  @Test
  public void testUndefinedVariable() {
    try {
      StatementBuilder.create().loadVariable("n").toJavaString();
      fail("Expected OutOfScopeException");
    }
    catch (OutOfScopeException oose) {
      // expected
    }
  }

  @Test
  public void testCreateAndInitializeArray() {
    try {
      StatementBuilder.create().newArray(Annotation.class)
          .initialize("1", "2")
          .toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException oose) {
      // expected
    }

    try {
      StatementBuilder.create().newArray(String.class).toJavaString();
      fail("Expected RuntimeException");
    }
    catch (Exception e) {
      // expected
      assertEquals("Must provide either dimension expressions or an array initializer", e.getMessage());
    }

    String s = StatementBuilder.create().newArray(String.class).initialize("1", "2").toJavaString();
    assertEquals("new String[] {\"1\",\"2\"}", s);

    Statement annotation1 = ObjectBuilder.newInstanceOf(Annotation.class)
        .extend()
        .publicOverridesMethod("annotationType")
        .append(StatementBuilder.create().load(Inject.class).returnValue())
        .finish()
        .finish();

    Statement annotation2 = ObjectBuilder.newInstanceOf(Annotation.class)
        .extend()
        .publicOverridesMethod("annotationType")
        .append(StatementBuilder.create().load(PostConstruct.class).returnValue())
        .finish()
        .finish();

    s = StatementBuilder.create().newArray(Annotation.class)
        .initialize(annotation1, annotation2)
        .toJavaString();

    assertEquals("new java.lang.annotation.Annotation[] {" +
        "new java.lang.annotation.Annotation() {\n" +
        " public Class annotationType() {\n" +
        "   return javax.inject.Inject.class;\n" +
        " }\n" +
        "}\n" +
        ",new java.lang.annotation.Annotation() {\n" +
        "   public Class annotationType() {\n" +
        "     return javax.annotation.PostConstruct.class;\n" +
        "   }\n" +
        " }\n" +
        "}", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeMultiDimensionalArray() {

    String s = StatementBuilder.create().newArray(Integer.class)
        .initialize(new Integer[][] { { 1, 2 }, { 3, 4 } })
        .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Integer[][] {{1,2},{3,4}}", s);

    s = StatementBuilder.create().newArray(String.class)
        .initialize(new Statement[][] {
            { StatementBuilder.create().invokeStatic(Integer.class, "toString", 1),
                StatementBuilder.create().invokeStatic(Integer.class, "toString", 2) },
            { StatementBuilder.create().invokeStatic(Integer.class, "toString", 3),
                StatementBuilder.create().invokeStatic(Integer.class, "toString", 4) } })
        .toJavaString();

    assertEquals("Failed to generate two dimensional array using statements",
        "new String[][] {{Integer.toString(1),Integer.toString(2)}," +
            "{Integer.toString(3),Integer.toString(4)}}", s);

    s = StatementBuilder.create().newArray(String.class)
        .initialize(new Object[][] {
            { StatementBuilder.create().invokeStatic(Integer.class, "toString", 1), "2" },
            { StatementBuilder.create().invokeStatic(Integer.class, "toString", 3), "4" } })
        .toJavaString();

    assertEquals("Failed to generate two dimensional array using statements and objects",
        "new String[][] {{Integer.toString(1),\"2\"}," +
            "{Integer.toString(3),\"4\"}}", s);

    s = StatementBuilder.create().newArray(String.class)
        .initialize(new String[][][] { { { "1", "2" }, { "a", "b" } }, { { "3", "4" }, { "b", "c" } } })
        .toJavaString();

    assertEquals("Failed to generate three dimensional array",
        "new String[][][] {{{\"1\",\"2\"},{\"a\",\"b\"}},{{\"3\",\"4\"},{\"b\",\"c\"}}}", s);
  }

  @Test
  public void testAssignArrayVariable() {
    String s = StatementBuilder.create()
        .addVariable("twoDimArray", String[][].class)
        .loadVariable("twoDimArray")
        .assignArrayValue("test", 1, 2)
        .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[1][2] = \"test\"", s);

    s = StatementBuilder.create()
        .addVariable("twoDimArray", String[][].class)
        .loadVariable("twoDimArray")
        .assignArrayValue(AssignmentOperator.PreIncrementAssign, "test", 1, 2)
        .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[1][2] += \"test\"", s);

    s = StatementBuilder.create()
        .addVariable("twoDimArray", String[][].class)
        .addVariable("i", int.class)
        .addVariable("j", int.class)
        .loadVariable("twoDimArray")
        .assignArrayValue("test", Variable.get("i"), Variable.get("j"))
        .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[i][j] = \"test\"", s);

    try {
      StatementBuilder.create()
          .addVariable("twoDimArray", String.class)
          .loadVariable("twoDimArray")
          .assignArrayValue("test", 1, 2)
          .toJavaString();
      fail("Expected InvalidTypeExcpetion");
    }
    catch (InvalidTypeException ite) {
      // Expected, variable is not an array.
    }

    try {
      StatementBuilder.create()
          .addVariable("twoDimArray", String[][].class)
          .addVariable("i", float.class)
          .addVariable("j", float.class)
          .loadVariable("twoDimArray")
          .assignArrayValue("test", Variable.get("i"), Variable.get("j"))
          .toJavaString();
      fail("Expected InvalidTypeExcpetion");
    }
    catch (InvalidTypeException ite) {
      // Expected, indexes are no integers
    }
  }
}