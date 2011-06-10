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
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidExpressionException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Bool;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.junit.Test;

import javax.enterprise.util.TypeLiteral;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the generation of loops using the {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilderTest extends AbstractStatementBuilderTest implements LoopBuilderTestResult {

  @Test
  public void testForeachLoop() throws Exception {
    Statement createObject = StatementBuilder.create()
        .newObject(String.class);

    Statement createAnotherObject = StatementBuilder.create()
        .newObject(Object.class);

    String foreachWithListOfStrings = StatementBuilder.create()
        .addVariable("list", new TypeLiteral<List<String>>() {})
        .loadVariable("list")
        .foreach("element")
        .finish().toJavaString();

    String foreachWithStringArray = StatementBuilder.create()
        .addVariable("list", String[].class)
        .loadVariable("list")
        .foreach("element")
        .append(createObject)
        .finish()
        .toJavaString();

    String foreachWithList = StatementBuilder.create()
        .addVariable("list", List.class)
        .loadVariable("list")
        .foreach("element")
        .append(createObject)
        .append(createAnotherObject)
        .finish().toJavaString();

    assertEquals("failed to generate foreach loop using a List<String>",
        FOREACH_RESULT_STRING_IN_LIST, foreachWithListOfStrings);
    assertEquals("failed to generate foreach loop using a String[]",
        FOREACH_RESULT_STRING_IN_ARRAY_ONE_STATEMENT, foreachWithStringArray);
    assertEquals("failed to generate foreach loop using a List<?>",
        FOREACH_RESULT_OBJECT_IN_LIST_TWO_STATEMENTS, foreachWithList);
  }

  @Test
  public void testForeachLoopWithUndefinedCollection() throws Exception {
    try {
      StatementBuilder.create()
          .loadVariable("list")
          .foreach("element", Integer.class)
          .finish().toJavaString();

      fail("Expected OutOfScopeException");
    }
    catch (OutOfScopeException oose) {
      // expected
    }
  }

  @Test
  public void testForeachLoopWithProvidedLoopVarType() throws Exception {
    Builder builder = StatementBuilder.create()
        .addVariable("list", new TypeLiteral<List<String>>() {})
        .loadVariable("list")
        .foreach("element", Object.class)
        .finish();

    assertEquals("failed to generate foreach loop with provided loop var type",
        FOREACH_RESULT_OBJECT_IN_LIST, builder.toJavaString());

    try {
      StatementBuilder.create()
          .addVariable("list", new TypeLiteral<List<String>>() {})
          .loadVariable("list")
          .foreach("element", Integer.class)
          .finish().toJavaString();

      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ite) {
      // expected
    }
  }

  @Test
  public void testNestedForeachLoops() throws Exception {
    Statement createObject = StatementBuilder.create().newObject(String.class);

    Builder outerLoop = StatementBuilder.create()
        .addVariable("list", new TypeLiteral<List<String>>() {})
        .loadVariable("list")
        .foreach("element")
        .append(StatementBuilder.create(
            ContextBuilder.create().addVariable(Variable.create("anotherList",
                new TypeLiteral<List<String>>() {})).getContext())
            .loadVariable("anotherList")
            .foreach("anotherElement")
            .append(createObject)
            .finish()
        ).finish();

    assertEquals("failed to generate nested foreach loops",
        FOREACH_RESULT_NESTED_STRING_IN_LIST, outerLoop.toJavaString());
  }

  @Test
  public void testForeachLoopWithInvalidCollectionType() throws Exception {

    try {
      StatementBuilder.create()
          .addVariable("list", String.class)
          .loadVariable("list")
          .foreach("element")
          .finish().toJavaString();

      fail("Expected TypeNotIterableException");
    }
    catch (TypeNotIterableException tnie) {
      // expected
    }
  }

  @Test
  public void testForeachLoopWithInvoke() throws Exception {
    Builder loop = StatementBuilder.create()
        .addVariable("map", Map.class)
        .loadVariable("map")
        .invoke("keySet")
        .foreach("key").finish();

    assertEquals("failed to generate foreach loop using invoke()",
        FOREACH_RESULT_KEYSET_LOOP, loop.toJavaString());
  }

  @Test
  public void testForeachLoopWithLiterals() throws Exception {
    LoopBuilder loopBuilder = StatementBuilder.create()
        .loadLiteral(new String[] { "s1", "s2" })
        .foreach("s")
        .append(StatementBuilder.create().loadVariable("s").invoke("getBytes"))
        .finish();

    assertEquals("failed to generate foreach loop using a literal String array",
        FOREACH_RESULT_LITERAL_STRING_ARRAY, loopBuilder.toJavaString());

    Context c = ContextBuilder.create().addVariable(Variable.create("s", String.class)).getContext();
    loopBuilder = StatementBuilder.create(c)
        .loadLiteral(new String[] { "s1", "s2" })
        .foreach("s")
        .append(StatementBuilder.create().loadVariable("s").invoke("getBytes"))
        .finish();

    assertEquals("failed to generate foreach loop using a literal String array",
        FOREACH_RESULT_LITERAL_STRING_ARRAY, loopBuilder.toJavaString());
  }

  @Test
  public void testWhileLoopWithInvalidExpressions() {
    try {
      StatementBuilder.create()
          .addVariable("n", Integer.class)
          .loadVariable("n")
          .while_().finish().toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ite) {
      // expected
    }

    try {
      StatementBuilder.create()
          .addVariable("str", String.class)
          .addVariable("str2", String.class)
          .loadVariable("str")
          .while_(BooleanOperator.GreaterThan, Variable.get("str2")).finish()
          .toJavaString();

      fail("Expected InvalidExpressionException");
    }
    catch (InvalidExpressionException e) {
      assertTrue("Wrong exception thrown", e.getMessage().contains(String.class.getName()));
    }
  }

  @Test
  public void testWhileLoop() {
    String s = StatementBuilder.create()
        .addVariable("b", Boolean.class)
        .loadVariable("b")
        .while_().finish().toJavaString();

    assertEquals("failed to generate empty while loop", WHILE_RESULT_EMPTY, s);

    s = StatementBuilder.create()
        .addVariable("b", Boolean.class)
        .loadVariable("b")
        .while_()
          .append(StatementBuilder.create().loadVariable("b").assignValue(false))
        .finish().toJavaString();

    assertEquals("failed to generate while loop with body", WHILE_RESULT_WITH_BODY, s);

    s = StatementBuilder.create()
        .addVariable("str", String.class)
        .loadVariable("str")
        .invoke("length")
        .while_(BooleanOperator.GreaterThanOrEqual, 2)
        .finish().toJavaString();

    assertEquals("failed to generate while loop with lhs invocation and body", WHILE_RESULT_RHS_EMPTY, s);
    
    s = StatementBuilder.create()
      .addVariable("str", String.class)
      .whileLoop(Bool.expr(
          Bool.expr(Variable.get("str"), BooleanOperator.NotEquals, null), 
          BooleanOperator.And,
          Bool.expr(Stmt.create().loadVariable("str").invoke("length"), BooleanOperator.GreaterThan, 0)))
      .finish().toJavaString();

    assertEquals("failed to generate while loop with nested expression", WHILE_RESULT_NESTED_RHS_EMPTY, s);
  }
}
