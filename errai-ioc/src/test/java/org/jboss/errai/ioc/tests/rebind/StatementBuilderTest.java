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

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.ExposeEntity;
import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Cast;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.literal.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.jboss.errai.ioc.tests.rebind.model.Foo;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link StatementBuilder} API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testDeclareVariableWithExactTypeProvided() {
    Context ctx = Context.create();
    String s = StatementBuilder.create().declareVariable("n", Integer.class, 10).generate(ctx);

    assertEquals("failed to generate variable declaration with type provided",
            "Integer n = 10", s);

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
  }

  @Test
  public void testDeclareVariableWithIntegerTypeInference() {
    Context ctx = Context.create();
    String s = StatementBuilder.create().declareVariable("n", 10).generate(ctx);

    assertEquals("failed to generate variable declaration with Integers type inference",
            "Integer n = 10", s);

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());
  }

  @Test
  public void testDeclareVariableWithStringTypeInference() {
    Context ctx = Context.create();
    String s = StatementBuilder.create().declareVariable("n", "10").generate(ctx);

    assertEquals("failed to generate variable declaration with =String type inference",
            "String n = \"10\"", s);

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral("10"), n.getValue());
  }

  @Test
  public void testDeclareVariableWithImplicitTypeConversion() {
    Context ctx = Context.create();
    String s = StatementBuilder.create().declareVariable("n", Integer.class, "10").generate(ctx);

    assertEquals("failed to generate variable declaration with implicit type conversion",
            "Integer n = 10", s);

    VariableReference n = ctx.getVariable("n");
    assertEquals("Wrong variable name", "n", n.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(Integer.class), n.getType());
    Assert.assertEquals("Wrong variable value", LiteralFactory.getLiteral(10), n.getValue());

    try {
      StatementBuilder.create().declareVariable("n", Integer.class, "abc").toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException ive) {
      // expected
      assertTrue(ive.getCause() instanceof NumberFormatException);
    }
  }
  
  @Test
  public void testDeclareVariableWithObjectInitializationWithExactTypeProvided() {
    Context ctx = Context.create();
    String s = StatementBuilder.create().declareVariable("str", String.class,
            ObjectBuilder.newInstanceOf(String.class)).generate(ctx);

    assertEquals("failed to generate variable declaration with object initialization and type provided",
            "String str = new String()", s);

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }

  @Test
  public void testDeclareVariableWithObjectInitializationWithStringTypeInference() {
    Context ctx = Context.create();
    String s = StatementBuilder.create(ctx)
            .declareVariable("str", ObjectBuilder.newInstanceOf(String.class)).toJavaString();

    assertEquals("failed to generate variable declaration with object initialization and string type inference",
            "String str = new String()", s);

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }
  
  @Test
  public void testDeclareVariableWithStatementInitialization() {
    Context ctx = Context.create();
    String s = Stmt.create().declareVariable("str", String.class,
            Stmt.create().nestedCall(Stmt.create().newObject(Integer.class).withParameters(2)).invoke("toString"))
        .generate(ctx);

    assertEquals("failed to generate variable declaration with statement initialization",
            "String str = (new Integer(2)).toString()", s);

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }

  @Test
  public void testDeclareFinalVariable() {
    Context ctx = Context.create();
    String s = StatementBuilder.create(ctx)
            .declareVariable(String.class).asFinal().named("str").initializeWith("10").toJavaString();

    assertEquals("failed to generate final variable declaration", "final String str = \"10\"", s);

    VariableReference str = ctx.getVariable("str");
    assertEquals("Wrong variable name", "str", str.getName());
    Assert.assertTrue("Variable should be final", ctx.getVariables().get("str").isFinal());
    Assert.assertEquals("Wrong variable type", MetaClassFactory.get(String.class), str.getType());
  }

  @Test
  public void testLoadUndefinedVariable() {
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
    String s = StatementBuilder.create().newArray(String.class).initialize("1", "2").toJavaString();
    assertEquals("Failed to generate 1-dimensional String array", "new String[] { \"1\", \"2\" }", s);
  }

  @Test
  public void testCreateAndInitializeArrayWithInvalidInitialization() {
    try {
      StatementBuilder.create().newArray(Annotation.class)
              .initialize("1", "2")
              .toJavaString();
      fail("Expected InvalidTypeException");
    }
    catch (InvalidTypeException oose) {
      // expected
    }
  }

  @Test
  public void testCreateAndInitializeArrayWithMissingInitializationAndDimensions() {
    try {
      StatementBuilder.create().newArray(String.class).toJavaString();
      fail("Expected RuntimeException");
    }
    catch (Exception e) {
      // expected
      assertEquals("Wrong exception details",
              "Must provide either dimension expressions or an array initializer", e.getMessage());
    }
  }

  @Test
  public void testCreateAndInitializeAnnotationArray() {
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

    String s = StatementBuilder.create().newArray(Annotation.class)
            .initialize(annotation1, annotation2)
            .toJavaString();

    assertEquals("failed to generate Annotation array",
            "new java.lang.annotation.Annotation[] { " +
                    "new java.lang.annotation.Annotation() {\n" +
                    " public Class annotationType() {\n" +
                    "   return javax.inject.Inject.class;\n" +
                    " }\n" +
                    "}" +
                    ", new java.lang.annotation.Annotation() {\n" +
                    "   public Class annotationType() {\n" +
                    "     return javax.annotation.PostConstruct.class;\n" +
                    "   }\n" +
                    " }\n" +
                    "}", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalArray() {
    String s = StatementBuilder.create().newArray(Integer.class)
            .initialize(new Integer[][] { { 1, 2 }, { 3, 4 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Integer[][] { { 1, 2 }, { 3, 4 } }", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalArrayWithSingleValue() {
    String s = StatementBuilder.create().newArray(Integer.class)
            .initialize(new Object[][] { { 1, 2 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Integer[][] { { 1, 2 } }", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalObjectArrayWithIntegers() {
    String s = StatementBuilder.create().newArray(Object.class)
            .initialize(new Object[][] { { 1, 2 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Object[][] { { 1, 2 } }", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalArrayWithStatements() {
    String s = StatementBuilder.create().newArray(String.class)
            .initialize(new Statement[][] {
                    { StatementBuilder.create().invokeStatic(Integer.class, "toString", 1),
                            StatementBuilder.create().invokeStatic(Integer.class, "toString", 2) },
                    { StatementBuilder.create().invokeStatic(Integer.class, "toString", 3),
                            StatementBuilder.create().invokeStatic(Integer.class, "toString", 4) } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array using statements",
            "new String[][] { { Integer.toString(1), Integer.toString(2) }, " +
                    "{ Integer.toString(3), Integer.toString(4) } }", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalArrayWithStatementsAndLiterals() {
    String s = StatementBuilder.create().newArray(String.class)
            .initialize(new Object[][] {
                    { StatementBuilder.create().invokeStatic(Integer.class, "toString", 1), "2" },
                    { StatementBuilder.create().invokeStatic(Integer.class, "toString", 3), "4" } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array using statements and objects",
            "new String[][] { { Integer.toString(1), \"2\" }," +
                    " { Integer.toString(3), \"4\" } }", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeThreeDimensionalArray() {
    String s = StatementBuilder.create().newArray(String.class)
            .initialize(new String[][][] { { { "1", "2" }, { "a", "b" } }, { { "3", "4" }, { "b", "c" } } })
            .toJavaString();

    assertEquals("Failed to generate three dimensional array",
            "new String[][][] { { { \"1\", \"2\" }, { \"a\", \"b\" } }, { { \"3\", \"4\" }, { \"b\", \"c\" } } }", s);
  }

  @Test
  public void testAssignArrayValue() {
    String s = StatementBuilder.create()
            .declareVariable("twoDimArray", String[][].class)
            .loadVariable("twoDimArray", 1, 2)
            .assignValue("test")
            .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[1][2] = \"test\"", s);
  }

  @Test
  public void testAssignArrayValueWithPreIncrementAssignment() {
    String s = StatementBuilder.create()
            .declareVariable("twoDimArray", String[][].class)
            .loadVariable("twoDimArray", 1, 2)
            .assignValue(AssignmentOperator.PreIncrementAssign, "test")
            .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[1][2] += \"test\"", s);
  }

  @Test
  public void testAssignArrayValueWithVariableIndexes() {
    String s = StatementBuilder.create()
            .declareVariable("twoDimArray", String[][].class)
            .declareVariable("i", int.class)
            .declareVariable("j", int.class)
            .loadVariable("twoDimArray", Variable.get("i"), Variable.get("j"))
            .assignValue("test")
            .toJavaString();

    assertEquals("Failed to generate array assignment", "twoDimArray[i][j] = \"test\"", s);
  }

  @Test
  public void testAssignArrayValueWithInvalidArray() {
    try {
      StatementBuilder.create()
              .declareVariable("twoDimArray", String.class)
              .loadVariable("twoDimArray", 1, 2)
              .assignValue("test")
              .toJavaString();
      fail("Expected InvalidTypeExcpetion");
    }
    catch (InvalidTypeException ite) {
      // Expected, variable is not an array.
    }
  }

  @Test
  public void testAssignArrayValueWithInvalidIndexType() {
    try {
      StatementBuilder.create()
              .declareVariable("twoDimArray", String[][].class)
              .declareVariable("i", float.class)
              .declareVariable("j", float.class)
              .loadVariable("twoDimArray", Variable.get("i"), Variable.get("j"))
              .assignValue("test")
              .toJavaString();
      fail("Expected InvalidTypeExcpetion");
    }
    catch (InvalidTypeException ite) {
      // Expected, indexes are no integers
    }
  }

  @Test
  public void testObjectCreationWithLiteralParameter() {
    String s = StatementBuilder.create().newObject(String.class).withParameters("original").toJavaString();
    assertEquals("failed to generate new object with parameters", "new String(\"original\")", s);
  }

  @Test
  public void testObjectCreationWithVariableParameter() {
    String s = StatementBuilder.create()
        .declareVariable("original", String.class)
        .newObject(String.class).withParameters(Variable.get("original")).toJavaString();
    assertEquals("failed to generate new object with parameters", "new String(original)", s);
  }

  @Test
  public void testObjectCreationWithParameterizedType() {
    String s = StatementBuilder.create().newObject(new TypeLiteral<List<String>>() {}).toJavaString();
    assertEquals("failed to generate new object with parameterized type", "new java.util.List<String>()", s);
  }

  @Test
  public void testObjectCreationWithAutoImportedParameterizedType() {
    Context c = Context.create().autoImport();
    String s = StatementBuilder.create(c).newObject(new TypeLiteral<List<Date>>() {}).toJavaString();
    assertEquals("failed to generate new object with parameterized type", "new List<Date>()", s);
  }

  @Test
  public void testObjectCreationWithParameterizedTypeAndClassImport() {
    Context c = Context.create().addClassImport(MetaClassFactory.get(List.class));
    String s = StatementBuilder.create(c).newObject(new TypeLiteral<List<String>>() {}).toJavaString();
    assertEquals("failed to generate new object with parameterized type", "new List<String>()", s);
  }

  @Test
  public void testObjectCreationWithFullyQualifiedParameterizedTypeAndClassImport() {
    Context c = Context.create().addClassImport(MetaClassFactory.get(List.class));
    String s = StatementBuilder.create(c).newObject(new TypeLiteral<List<Date>>() {}).toJavaString();
    assertEquals("failed to generate new object with parameterized type", "new List<java.util.Date>()", s);
  }

  @Test
  public void testObjectCreationWithNestedParameterizedTypeAndClassImports() {
    Context c = Context.create()
            .addClassImport(MetaClassFactory.get(List.class))
            .addClassImport(MetaClassFactory.get(Map.class));

    String s = StatementBuilder.create(c)
            .newObject(new TypeLiteral<List<List<Map<String, Integer>>>>() {}).toJavaString();
    assertEquals("failed to generate new object with parameterized type", "new List<List<Map<String, Integer>>>()", s);
  }

  @Test
  public void testThrowExceptionUsingNewInstance() {
    Context c = Context.create().autoImport();
    String s = StatementBuilder.create(c).throw_(InvalidTypeException.class).toJavaString();
    assertEquals("failed to generate throw statement using a new instance",
            "throw new InvalidTypeException()", s);
  }

  @Test
  public void testThrowExceptionUsingNewInstanceWithParameters() {
    Context c = Context.create().autoImport();
    String s = StatementBuilder.create(c).throw_(InvalidTypeException.class, "message").toJavaString();
    assertEquals("failed to generate throw statement using a new instance",
            "throw new InvalidTypeException(\"message\")", s);
  }

  @Test
  public void testThrowExceptionUsingVariable() {
    String s = StatementBuilder.create().declareVariable("t", Throwable.class).throw_("t").toJavaString();
    assertEquals("failed to generate throw statement using a variable", "throw t", s);
  }

  @Test
  public void testThrowExceptionUsingInvalidVariable() {
    try {
      StatementBuilder.create().declareVariable("t", Integer.class).throw_("t").toJavaString();
      fail("expected InvalidTypeException");
    }
    catch (InvalidTypeException e) {
      // expected
    }
  }

  @Test
  public void testThrowExceptionUsingUndefinedVariable() {
    try {
      StatementBuilder.create().throw_("t").toJavaString();
      fail("expected OutOfScopeException");
    }
    catch (OutOfScopeException e) {
      // expected
    }
  }

  @Test
  public void testNestedCall() {
    String s = StatementBuilder.create()
        .nestedCall(
            StatementBuilder.create().declareVariable("n", Integer.class).loadVariable("n").invoke("toString"))
        .invoke("getBytes")
        .toJavaString();

    assertEquals("failed to generate nested call", "(n.toString()).getBytes()", s);
  }

  @Test
  public void testAssignField() {
    String s = Stmt.create(Context.create().autoImport()).nestedCall(Stmt.create()
            .newObject(Foo.class)).loadField("bar").loadField("name").assignValue("test").toJavaString();

    assertEquals("failed to generate nested field assignment", 
        "(new Foo()).bar.name = \"test\"", s);
  }
  
  @Test
  public void testCastDown() {
    Statement stmt = Cast.to(String.class, Stmt.create().declareVariable("obj", Object.class).loadVariable("obj"));
    assertEquals("failed to generate cast", "(String) obj", stmt.generate(Context.create()));
  }
  
  @Test
  public void testCastUp() {
    Statement stmt = Cast.to(Object.class, Stmt.create().declareVariable("str", String.class).loadVariable("str"));
    assertEquals("failed to generate cast", "(Object) str", stmt.generate(Context.create()));
  }
  
  @Test
  public void testInvalidCast() {
    try {
      Statement stmt = Cast.to(Integer.class, Stmt.create().declareVariable("str", String.class).loadVariable("str"));
      stmt.generate(Context.create());
      fail("expected InvalidTypeException");
    } catch(InvalidTypeException e) {
      //expected
      assertEquals("Wrong exception message", "java.lang.String cannot be cast to java.lang.Integer", e.getMessage());
    }
  }
}