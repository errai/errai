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

import java.io.Serializable;

import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.codegen.Parameter;
import org.jboss.errai.ioc.rebind.ioc.codegen.ThrowsDeclaration;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClassBuilderTest extends AbstractStatementBuilderTest implements ClassBuilderTestResult {
  
  @Test
  public void testDefineClassImplementingInterface() {
    String cls = ClassBuilder.define("org.foo.Bar")
        .publicScope().implementsInterface(Serializable.class)
        .body()
        .privateField("name", String.class)
        .finish()
        .toJavaString();

    assertEquals("failed to generate class definition implementing an interface", CLASS_IMPLEMENTING_INTERFACE, cls);
  }

  @Test
  public void testDefineClassWithAccessorMethods() {
    String cls = ClassBuilder.define("org.foo.Foo")
        .publicScope()
        .body()
        .privateField("name", String.class)
        .initializesWith(Stmt.create().load("default"))
        .finish()
        .publicMethod(String.class, "getName")
        .append(Stmt.create().loadVariable("name").returnValue())
        .finish()
        .publicMethod(void.class, "setName", Parameter.of(String.class, "name"))
        .append(Stmt.create().loadClassMember("name").assignValue(Variable.get("name")))
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
        .initializesWith(Stmt.create().load("default"))
        .finish()
        .publicMethod(String.class, "getName")
        .append(Stmt.create().loadVariable("name").returnValue())
        .finish()
        .publicMethod(void.class, "setName", Parameter.of(String.class, "name"))
        .append(Stmt.create().loadVariable("this.name").assignValue(Variable.get("name")))
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
  
  /*@Test
  public void testDefineClassWithConstructorCallingSuper() {
    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .abstractClass()
        .body()
        .publicConstructor()
        //.append(Stmt.create().)
        .finish()
        .toJavaString();
    
    assertEquals("failed to generate abstract class", CLASS_WITH_CONSTRUCTOR_CALLING_SUPER, cls);
  }*/
  
  @Test
  public void testDefineClassWithMethodWithThrowsDeclaration() {
    
    String cls = ClassBuilder
        .define("org.foo.Foo")
        .publicScope()
        .body()
        .publicMethod(void.class, "initialize", ThrowsDeclaration.of(Exception.class, IllegalArgumentException.class))
        .finish()
        .toJavaString();

    assertEquals("failed to generate abstract class", CLASS_WITH_METHOD_WITH_THROWS_DECLARATION, cls);
  }
  
  @Test
  public void testDefineClass() {
    String cls = ClassBuilder.implement(Bootstrapper.class)
        .publicMethod(InterfaceInjectionContext.class, "bootstrapContainer")
        .append(Stmt.create().addVariable("ctx", Stmt.create().newObject(InterfaceInjectionContext.class)))
        .append(Stmt.create().loadVariable("ctx").returnValue())
        .finish().toJavaString();

    assertEquals("package org.jboss.errai.ioc.client.api;\n" +
        "\n" +
        "import org.jboss.errai.ioc.client.api.Bootstrapper;\n" +
        "import org.jboss.errai.ioc.client.InterfaceInjectionContext;\n" +
        "\n" +
        "public class BootstrapperImpl implements Bootstrapper {\n" +
        "    public InterfaceInjectionContext bootstrapContainer() {\n" +
        "        InterfaceInjectionContext ctx = new InterfaceInjectionContext();\n" +
        "        return ctx;\n" +
        "    }\n" +
        "}", cls);
  }
}