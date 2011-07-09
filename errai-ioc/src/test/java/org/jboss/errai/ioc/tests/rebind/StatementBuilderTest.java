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

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
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
import org.jboss.errai.ioc.rebind.ioc.codegen.util.PrettyPrinter;
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
    assertEquals("Failed to generate 1-dimensional String array", "new String[] {\"1\",\"2\"}", s);
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
            "new java.lang.annotation.Annotation[] {" +
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
  public void testCreateAndInitializeTwoDimensionalArray() {
    String s = StatementBuilder.create().newArray(Integer.class)
            .initialize(new Integer[][] { { 1, 2 }, { 3, 4 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Integer[][] {{1,2},{3,4}}", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalArrayWithSingleValue() {
    String s = StatementBuilder.create().newArray(Integer.class)
            .initialize(new Object[][] { { 1, 2 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Integer[][] {{1,2}}", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeTwoDimensionalObjectArrayWithIntegers() {
    String s = StatementBuilder.create().newArray(Object.class)
            .initialize(new Object[][] { { 1, 2 } })
            .toJavaString();

    assertEquals("Failed to generate two dimensional array", "new Object[][] {{1,2}}", s);
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
            "new String[][] {{Integer.toString(1),Integer.toString(2)}," +
                    "{Integer.toString(3),Integer.toString(4)}}", s);
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
            "new String[][] {{Integer.toString(1),\"2\"}," +
                    "{Integer.toString(3),\"4\"}}", s);
  }

  @Test
  @SuppressWarnings(value = { "all" })
  public void testCreateAndInitializeThreeDimensionalArray() {
    String s = StatementBuilder.create().newArray(String.class)
            .initialize(new String[][][] { { { "1", "2" }, { "a", "b" } }, { { "3", "4" }, { "b", "c" } } })
            .toJavaString();

    assertEquals("Failed to generate three dimensional array",
            "new String[][][] {{{\"1\",\"2\"},{\"a\",\"b\"}},{{\"3\",\"4\"},{\"b\",\"c\"}}}", s);
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
                StatementBuilder.create().declareVariable("n", Integer.class).loadVariable("n").invoke(
                    "toString"))
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
  public void testZZZ() {

    System.out.println(PrettyPrinter.prettyPrintJava("package org.jboss.errai.ioc.client.api;\n" +
            "\n" +
            "import org.jboss.errai.ioc.client.api.Bootstrapper;\n" +
            "import org.jboss.errai.cdi.client.EventProducerTestModule;\n" +
            "import org.jboss.errai.cdi.client.api.Event;\n" +
            "import org.jboss.errai.cdi.client.EventObserverTestModule;\n" +
            "import org.jboss.errai.ioc.client.InterfaceInjectionContext;\n" +
            "import org.jboss.errai.cdi.client.ProducerTestModule;\n" +
            "import org.jboss.errai.cdi.client.EventProvider;\n" +
            "import org.jboss.errai.ioc.client.api.BootstrapperImpl;\n" +
            "import java.lang.annotation.Annotation;\n" +
            "import org.jboss.errai.ioc.client.api.builtin.MessageBusProvider;\n" +
            "import org.jboss.errai.bus.client.api.MessageCallback;\n" +
            "import org.jboss.errai.bus.client.api.Message;\n" +
            "import java.util.HashSet;\n" +
            "import java.util.Set;\n" +
            "import org.jboss.errai.cdi.client.CDIProtocol;\n" +
            "import org.jboss.errai.cdi.client.events.BusReadyEvent;\n" +
            "import org.jboss.errai.cdi.client.event.ReceivedEvent;\n" +
            "import org.jboss.errai.cdi.client.event.StartEvent;\n" +
            "\n" +
            "public class BootstrapperImpl implements Bootstrapper {\n" +
            "    private native static void EventProducerTestModule_event(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::event = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_event(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::event;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventA(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventA = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventA(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventA;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventB(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventB = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventB(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventB;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventC(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventC = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventC(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventC;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventAB(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventAB = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventAB(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventAB;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventBC(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventBC = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventBC(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventBC;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventAC(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventAC = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventAC(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventAC;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventProducerTestModule_eventABC(EventProducerTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventABC = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventProducerTestModule_eventABC(EventProducerTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventProducerTestModule::eventABC;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static void EventObserverTestModule_startEvent(EventObserverTestModule instance, Event value) /*-{\n" +
            "        instance.@org.jboss.errai.cdi.client.EventObserverTestModule::startEvent = value;\n" +
            "    }-*/;\n" +
            "\n" +
            "    private native static Event EventObserverTestModule_startEvent(EventObserverTestModule instance) /*-{\n" +
            "        return instance.@org.jboss.errai.cdi.client.EventObserverTestModule::startEvent;\n" +
            "    }-*/;\n" +
            "\n" +
            "    public InterfaceInjectionContext bootstrapContainer() {\n" +
            "        InterfaceInjectionContext ctx = new InterfaceInjectionContext();\n" +
            "        final ProducerTestModule inj9 = new ProducerTestModule();\n" +
            "        inj9.doPostConstruct();\n" +
            "        final EventProducerTestModule inj10 = new EventProducerTestModule();\n" +
            "        final EventProvider inj4 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_event(inj10, (inj4).provide(new Class[] {        String.class        }, null));\n" +
            "        final EventProvider inj12 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventA(inj10, (inj12).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.A() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.A()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.A.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj13 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventB(inj10, (inj13).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.B() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.B()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.B.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj14 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventC(inj10, (inj14).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.C() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.C()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.C.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj15 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventAB(inj10, (inj15).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.B() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.B()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.B.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            , new org.jboss.errai.cdi.client.qualifier.A() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.A()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.A.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj16 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventBC(inj10, (inj16).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.B() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.B()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.B.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            , new org.jboss.errai.cdi.client.qualifier.C() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.C()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.C.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj17 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventAC(inj10, (inj17).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.A() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.A()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.A.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            , new org.jboss.errai.cdi.client.qualifier.C() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.C()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.C.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final EventProvider inj18 = new EventProvider();\n" +
            "        BootstrapperImpl.EventProducerTestModule_eventABC(inj10, (inj18).provide(new Class[] {        String.class        }, new Annotation[] {            new org.jboss.errai.cdi.client.qualifier.B() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.B()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.B.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            , new org.jboss.errai.cdi.client.qualifier.A() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.A()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.A.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            , new org.jboss.errai.cdi.client.qualifier.C() {\n" +
            "                public String toString() {\n" +
            "                    return \"@org.jboss.errai.cdi.client.qualifier.C()\";\n" +
            "                }\n" +
            "                public Class annotationType() {\n" +
            "                    return org.jboss.errai.cdi.client.qualifier.C.class;\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "        }));\n" +
            "        final MessageBusProvider inj8 = new MessageBusProvider();\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:org.jboss.errai.cdi.client.events.BusReadyEvent\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(BusReadyEvent.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj10.onBusReady((BusReadyEvent) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:org.jboss.errai.cdi.client.event.ReceivedEvent\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(ReceivedEvent.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj10.collectResults((ReceivedEvent) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        inj10.doPostConstruct();\n" +
            "        final EventObserverTestModule inj11 = new EventObserverTestModule();\n" +
            "        final EventProvider inj19 = new EventProvider();\n" +
            "        BootstrapperImpl.EventObserverTestModule_startEvent(inj11, (inj19).provide(new Class[] {        StartEvent.class        }, null));\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:org.jboss.errai.cdi.client.events.BusReadyEvent\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(BusReadyEvent.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onBusReady((BusReadyEvent) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEvent((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventA((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventB((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.C\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventC((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventAB((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventBA((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.C\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventAC((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.C\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventBC((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        ((inj8).provide()).subscribe(\"cdi.event:java.lang.String\", new MessageCallback() {\n" +
            "            public void callback(Message message) {\n" +
            "                Set methodQualifiers = new HashSet<String>();\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.B\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.A\");\n" +
            "                methodQualifiers.add(\"org.jboss.errai.cdi.client.qualifier.C\");\n" +
            "                Set qualifiers = message.get(Set.class, CDIProtocol.QUALIFIERS);\n" +
            "                if (methodQualifiers.equals((HashSet) qualifiers) || ((qualifiers == null) && methodQualifiers.isEmpty())) {\n" +
            "                    Object response = message.get(String.class, CDIProtocol.OBJECT_REF);\n" +
            "                    inj11.onEventABC((String) response);\n" +
            "                };\n" +
            "            }\n" +
            "\n" +
            "        }\n" +
            "        );\n" +
            "        inj11.doPostConstruct();\n" +
            "        return ctx;\n" +
            "    }\n" +
            "}\n\n\n"));

  }
}