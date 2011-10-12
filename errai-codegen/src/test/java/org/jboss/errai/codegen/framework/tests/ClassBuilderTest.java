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

package org.jboss.errai.codegen.framework.tests;

import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Map;

import org.jboss.errai.codegen.framework.InnerClass;
import org.jboss.errai.codegen.framework.Modifier;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.exception.UndefinedMethodException;
import org.jboss.errai.codegen.framework.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.framework.tests.model.Baz;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.junit.Test;

/**
 * Tests the {@link ClassBuilder} API.
 * 
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClassBuilderTest extends AbstractStatementBuilderTest implements ClassBuilderTestResult {

  @Test
  public void testDefineClassImplementingInterface() {
    String cls = ClassBuilder.define("org.foo.Bar")
        .publicScope()
        .implementsInterface(Serializable.class)
        .body()
        .privateField("name", String.class)
        .finish()
        .toJavaString();

    assertEquals("failed to generate class definition implementing an interface", CLASS_IMPLEMENTING_INTERFACE, cls);
  }

  @Test
  public void testDefineClassImplementingMultipleInterfaces() {
    String cls = ClassBuilder.define("org.foo.Bar")
        .publicScope()
        .implementsInterface(Serializable.class)
        .implementsInterface(Cloneable.class)
        .body()
        .privateField("name", String.class)
        .finish()
        .toJavaString();

    assertEquals("failed to generate class definition implementing multiple interfaces",
        CLASS_IMPLEMENTING_MULTIPLE_INTERFACES, cls);
  }

  @Test
  public void testDefineInnerClass() {
    ClassStructureBuilder<?> innerClass = ClassBuilder.define("Inner")
        .packageScope()
        .implementsInterface(Serializable.class)
        .body()
        .privateField("name", String.class)
        .finish()
        .publicMethod(void.class, "setName", Parameter.of(String.class, "name"))
        .append(Stmt.loadClassMember("name").assignValue(Variable.get("name")))
        .finish();

    String cls = ClassBuilder.define("foo.bar.Baz")
        .publicScope()
        .body()
        .publicMethod(void.class, "someMethod")
        .append(new InnerClass((BuildMetaClass) innerClass.getClassDefinition()))
        .append(Stmt.newObject(innerClass.getClassDefinition()))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with method using inner class",
        CLASS_WITH_METHOD_USING_INNER_CLASS, cls);
  }

  @Test
  public void testDefineClassWithAccessorMethods() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .privateField("name", String.class)
        .initializesWith(Stmt.load("default"))
        .finish()
        .publicMethod(String.class, "getName")
        .append(Stmt.loadVariable("name").returnValue())
        .finish()
        .publicMethod(void.class, "setName", Parameter.of(String.class, "name"))
        .append(Stmt.loadClassMember("name").assignValue(Variable.get("name")))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class definition with accessor methods", CLASS_WITH_ACCESSOR_METHODS, cls);
  }

  @Test
  public void testDefineClassWithAccessorMethodsUsingThisKeyword() {
    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .privateField("name", String.class)
        .initializesWith(Stmt.load("default"))
        .finish()
        .publicMethod(String.class, "getName")
        .append(Stmt.loadVariable("name").returnValue())
        .finish()
        .publicMethod(void.class, "setName", Parameter.of(String.class, "name"))
        .append(Stmt.loadVariable("this.name").assignValue(Variable.get("name")))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class definition with accessor methods", CLASS_WITH_ACCESSOR_METHODS, cls);
  }

  @Test
  public void testDefineClassWithParent() {

    String cls = ClassBuilder
        .define("org.foo.Foo", String.class)
        .publicScope()
        .body()
        .publicConstructor(Parameter.of(int.class, "i"))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with parent", CLASS_WITH_PARENT, cls);
  }

  @Test
  public void testDefineAbstractClass() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .abstractClass()
        .body()
        .publicConstructor()
        .finish()
        .toJavaString();

    assertEquals("failed to generate abstract class", ABSTRACT_CLASS, cls);
  }

  @Test
  public void testDefineAbstractClassWithAbstractMethods() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .abstractClass()
        .body()
        .publicConstructor()
        .finish()
        .publicAbstractMethod(void.class, "foo")
        .finish()
        .protectedAbstractMethod(void.class, "bar")
        .finish()
        .publicMethod(void.class, "baz")
        .finish()
        .toJavaString();

    assertEquals("failed to generate abstract class with abstract method", ABSTRACT_CLASS_WITH_ABSTRACT_METHODS, cls);
  }

  @Test
  public void testDefineClassWithConstructorCallingSuper() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .publicConstructor()
        .callSuper()
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with constructor calling super()",
            CLASS_WITH_CONSTRUCTOR_CALLING_SUPER, cls);
  }

  @Test
  public void testDefineClassWithConstructorCallingThis() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .privateField("b", boolean.class)
        .finish()
        .publicConstructor()
        .callThis(false)
        .finish()
        .publicConstructor(Parameter.of(boolean.class, "b"))
        .append(Stmt.loadClassMember("b").assignValue(Variable.get("b")))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with constructor calling this()",
            CLASS_WITH_CONSTRUCTOR_CALLING_THIS, cls);
  }

  @Test
  public void testDefineClassWithMethodCallingMethodOnThis() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .publicMethod(void.class, "bar")
        .append(Stmt.loadVariable("this").invoke("foo"))
        .finish()
        .publicMethod(String.class, "foo")
        .append(Stmt.load(null).returnValue())
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with method calling method on this",
            CLASS_WITH_METHOD_CALLING_METHOD_ON_THIS, cls);
  }

  @Test
  public void testDefineClassWithMethodCallingInvalidMethodOnThis() {
    try {
      ClassBuilder.define("org.foo.Foo")
          .publicScope()
          .body()
          .publicMethod(void.class, "bar")
          .append(Stmt.loadVariable("this").invoke("foo", "invalidParam"))
          .finish()
          .publicMethod(String.class, "foo")
          .append(Stmt.load(null).returnValue())
          .finish()
          .toJavaString();
      fail("exprected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      // expected
      assertEquals("Wrong exception thrown", udme.getMethodName(), "foo");
    }
  }

  @Test
  public void testDefineClassWithMethodCallingMethodOnSuper() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .publicMethod(void.class, "bar")
        .append(Stmt.loadVariable("this").invoke("foo"))
        .finish()
        .publicMethod(String.class, "foo")
        .append(Stmt.loadVariable("super").invoke("toString").returnValue())
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with method calling method on this",
            CLASS_WITH_METHOD_CALLING_METHOD_ON_SUPER, cls);
  }

  @Test
  public void testDefineClassWithMethodCallingInvalidMethodOnSuper() {
    try {
      ClassBuilder.define("org.foo.Foo")
          .publicScope()
          .body()
          .publicMethod(void.class, "bar")
          .append(Stmt.loadVariable("this").invoke("foo"))
          .finish()
          .publicMethod(String.class, "foo")
          .append(Stmt.loadVariable("super").invoke("undefinedMethod"))
          .finish()
          .toJavaString();
      fail("exprected UndefinedMethodException");
    }
    catch (UndefinedMethodException udme) {
      // expected
      assertEquals("Wrong exception thrown", udme.getMethodName(), "undefinedMethod");
    }
  }

  @Test
  public void testDefineClassWithMethodHavingThrowsDeclaration() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .publicMethod(void.class, "initialize")
        .throws_(Exception.class, IllegalArgumentException.class)
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with method having throws declaration",
            CLASS_WITH_METHOD_HAVING_THROWS_DECLARATION, cls);
  }

  @Test
  public void testDefineClassWithMethodsOfAllScopes() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .publicMethod(void.class, "publicMethod")
        .finish()
        .protectedMethod(void.class, "protectedMethod")
        .finish()
        .packageMethod(void.class, "packagePrivateMethod")
        .finish()
        .privateMethod(void.class, "privateMethod")
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with methods of all scopes", CLASS_WITH_METHODS_OF_ALL_SCOPES, cls);
  }

  @Test
  public void testDefineClassWithFieldsOfAllScopes() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .publicField("publicField", int.class)
        .finish()
        .protectedField("protectedField", int.class)
        .finish()
        .packageField("packagePrivateField", int.class)
        .finish()
        .privateField("privateField", int.class)
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with fields of all scopes", CLASS_WITH_FIELDS_OF_ALL_SCOPES, cls);
  }

  @Test
  public void testDefineClassWithConstructorsOfAllScopes() {

    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .publicConstructor()
        .finish()
        .protectedConstructor()
        .finish()
        .packageConstructor()
        .finish()
        .privateConstructor()
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with constructors of all scopes",
            CLASS_WITH_CONSTRUCTORS_OF_ALL_SCOPES, cls);
  }

  @Test
  public void testDefineClassByImplementingInterface() {
    String cls = ClassBuilder.implement(Baz.class)
            .publicMethod(void.class, "someMethod")
            .finish().toJavaString();

    assertEquals("failed to generate class by implementing an interface",
            CLASS_DEFINITION_BY_IMPLEMENTING_INTERFACE, cls);
  }

  @Test
  public void testDefineClassWithStaticMethod() {
    String cls = ClassBuilder.define("my.test.Clazz")
        .publicScope().body()
        .publicMethod(void.class, "test").modifiers(Modifier.Static)
        .body()
        .append(Stmt.loadStatic(System.class, "out").invoke("println", "Hello, World!"))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with static method", CLASS_WITH_STATIC_METHOD, cls);
  }

  @Test
  public void testDefineClassWithJSNIMethod() {
    String cls = ClassBuilder.define("my.test.Clazz")
        .publicScope().body()
        .publicMethod(void.class, "test").modifiers(Modifier.JSNI)
        .body()
        .append(Stmt.loadStatic(System.class, "out").invoke("println", "Hello, World!"))
        .finish()
        .toJavaString();

    assertEquals("failed to generate class with JSNI method", CLASS_WITH_JSNI_METHOD, cls);
  }
}