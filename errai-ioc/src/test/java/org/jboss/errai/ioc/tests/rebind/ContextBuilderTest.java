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

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder} API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testDeclareVariable() {
    Statement declaration = ContextBuilder.create()
        .declareVariable("n", Integer.class)
        .initializeWith(10);

    assertEquals("failed to generate variable declaration using a literal initialization",
        "java.lang.Integer n = 10;", declaration.generate(Context.create()));

    declaration = ContextBuilder.create()
        .declareVariable("n")
        .initializeWith(10);

    assertEquals("failed to generate variable declaration using a literal initialization and type inference",
        "java.lang.Integer n = 10;", declaration.generate(Context.create()));

    declaration = ContextBuilder.create()
        .declareVariable("n")
        .initializeWith("10");

    assertEquals("failed to generate variable declaration using a literal initialization and type inference",
        "java.lang.String n = \"10\";", declaration.generate(Context.create()));

    declaration = ContextBuilder.create()
        .declareVariable("n", Integer.class)
        .initializeWith("10");

    assertEquals("failed to generate variable declaration using a literal initialization and type conversion",
        "java.lang.Integer n = 10;", declaration.generate(Context.create()));

    try {
      ContextBuilder.create()
          .declareVariable("n", Integer.class)
          .initializeWith("abc")
          .generate(Context.create());
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      //expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }

  @Test
  public void testDeclareVariableWithObjectInitialization() {
    Statement declaration = ContextBuilder.create()
        .declareVariable("str", String.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class));

    assertEquals("failed to generate variable declaration using an objectbuilder initialization",
        "java.lang.String str = new java.lang.String();", declaration.generate(Context.create()));

    declaration = ContextBuilder.create()
        .declareVariable("str", String.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"));

    assertEquals("failed to generate variable declaration using an objectbuilder initialization with parameters",
        "java.lang.String str = new java.lang.String(\"abc\");", declaration.generate(Context.create()));

    declaration = ContextBuilder.create()
        .declareVariable("str", Object.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"));

    assertEquals("failed to generate variable declaration using an objectbuilder initialization with parameters",
        "java.lang.Object str = new java.lang.String(\"abc\");", declaration.generate(Context.create()));

    try {
      ContextBuilder.create()
          .declareVariable("str", Integer.class)
          .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"));
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
    }

    try {
      ContextBuilder.create()
          .declareVariable("str", String.class)
          .initializeWith(ObjectBuilder.newInstanceOf(Object.class));
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
    }
  }

  @Test
  public void testAddVariableWithLiteralInitialization() {
    Context ctx = ContextBuilder.create().addVariable("n", Integer.class, 10).getContext();
    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    ctx = ContextBuilder.create().addVariable("n", 10).getContext();
    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    ctx = ContextBuilder.create().addVariable("n", "10").getContext();
    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());

    ctx = ContextBuilder.create().addVariable("n", Integer.class, "10").getContext();
    n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    try {
      ctx = ContextBuilder.create().addVariable("n", Integer.class, "abc").getContext();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      //expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }

  @Test
  public void testAddVariableWithObjectInitialization() {
    Context ctx = ContextBuilder.create().addVariable("str", String.class,
        ObjectBuilder.newInstanceOf(String.class)).getContext();

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());

    ctx = ContextBuilder.create().addVariable("str", ObjectBuilder.newInstanceOf(String.class)).getContext();

    str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }
}