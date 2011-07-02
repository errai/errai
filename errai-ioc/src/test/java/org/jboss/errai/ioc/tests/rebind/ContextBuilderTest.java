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
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ContextBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testDeclareVariableWithExactTypeProvided() {
    String declaration = ContextBuilder.create()
        .declareVariable("n", Integer.class)
        .initializeWith(10)
        .toJavaString();
      
    assertEquals("failed to generate variable declaration using a literal initialization",
        "Integer n = 10;", declaration);
  }

  @Test
  public void testDeclareVariableWithIntegerTypeInference() {
    String declaration = ContextBuilder.create()
        .declareVariable("n")
        .initializeWith(10)
        .toJavaString();

    assertEquals("failed to generate variable declaration using a literal initialization and type inference",
        "Integer n = 10;", declaration);
  }

  @Test
  public void testDeclareVariableWithStringTypeInference() {
    String declaration = ContextBuilder.create()
        .declareVariable("n")
        .initializeWith("10")
        .toJavaString();

    assertEquals("failed to generate variable declaration using a literal initialization and type inference",
        "String n = \"10\";", declaration);
  }

  @Test
  public void testDeclareVariableWithImplicitTypeConversion() {
    String declaration = ContextBuilder.create()
        .declareVariable("n", Integer.class)
        .initializeWith("10")
        .toJavaString();

    assertEquals("failed to generate variable declaration using a literal initialization and type conversion",
        "Integer n = 10;", declaration);
  }

  @Test
  public void testDeclareVariableWithInvalidInitialization() {
    try {
      ContextBuilder.create()
          .declareVariable("n", Integer.class)
          .initializeWith("abc")
          .toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }

  @Test
  public void testDeclareVariableWithObjectInitialization() {
    String declaration = ContextBuilder.create()
        .declareVariable("str", String.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class))
        .toJavaString();

    assertEquals("failed to generate variable declaration using an objectbuilder initialization",
        "String str = new String();", declaration);
  }

  @Test
  public void testDeclareVariableWithObjectInitializationWithParameters() {
    String declaration = ContextBuilder.create()
        .declareVariable("str", String.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"))
        .toJavaString();

    assertEquals("failed to generate variable declaration using an objectbuilder initialization with parameters",
        "String str = new String(\"abc\");", declaration);
  }

  @Test
  public void testDeclareVariableWithObjectInitializationUsingSuperClassType() {

    String declaration = ContextBuilder.create()
        .declareVariable("str", Object.class)
        .initializeWith(ObjectBuilder.newInstanceOf(String.class).withParameters("abc"))
        .toJavaString();

    assertEquals("failed to generate variable declaration using an objectbuilder initialization with parameters",
        "Object str = new String(\"abc\");", declaration);

    try {
      Stmt.create()
          .declareVariable("str", Integer.class, ObjectBuilder.newInstanceOf(String.class).withParameters("abc"))
          .toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
    }

    try {
      Stmt.create()
          .declareVariable("str", String.class, ObjectBuilder.newInstanceOf(Object.class))
          .toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
    }
  }

  @Test
  public void testAddVariableWithExactTypeProvided() {
    Context ctx = ContextBuilder.create().addVariable("n", Integer.class, 10).getContext();

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
  }

  @Test
  public void testAddVariableWithIntegerTypeInference() {
    Context ctx = ContextBuilder.create().addVariable("n", 10).getContext();

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
  }

  @Test
  public void testAddVariableWithStringTypeInference() {
    Context ctx = ContextBuilder.create().addVariable("n", "10").getContext();

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());
  }

  @Test
  public void testAddVariableWithImplicitTypeConversion() {
    Context ctx = ContextBuilder.create().addVariable("n", Integer.class, "10").getContext();

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    try {
      Stmt.create(ContextBuilder.create().addVariable("n", Integer.class, "abc").getContext()).toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }

  @Test
  public void testAddVariableWithObjectInitializationWithExactTypeProvided() {
    Context ctx = ContextBuilder.create().addVariable("str", String.class,
        ObjectBuilder.newInstanceOf(String.class)).getContext();

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }

  @Test
  public void testAddVariableWithObjectInitializationWithStringTypeInference() {
    Context ctx = ContextBuilder.create().addVariable("str", ObjectBuilder.newInstanceOf(String.class)).getContext();

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    // variable value cannot be verified before initialization statement was generated.
  }
}