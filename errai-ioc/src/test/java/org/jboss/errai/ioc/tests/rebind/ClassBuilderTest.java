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

import org.jboss.errai.ioc.client.InterfaceInjectionContext;
import org.jboss.errai.ioc.client.api.Bootstrapper;
import org.jboss.errai.ioc.rebind.ioc.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.util.Stmt;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class ClassBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testDefineClass() {
    String cls = ClassBuilder.define("org.foo.Bar")
            .publicScope().implementsInterface(Serializable.class)
            .body()
            .publicMethod(String.class, "getName")
            .append(Stmt.create().load("foobar").returnValue())
            .finish()
            .toJavaString();


    System.out.println(cls);

    assertEquals("package org.foo;\n" +
            "\n" +
            "import java.io.Serializable;\n" +
            "\n" +
            "public class Bar implements Serializable {\n" +
            "    public String getName() {\n" +
            "        return \"foobar\";\n" +
            "    }\n" +
            "}", cls);
  }

  @Test
  public void testDefineClassA() {
    String cls = ClassBuilder.define("org.foo.Foo")
            .publicScope()
            .body()
            .privateField("name", String.class)
            .initializesWith(Stmt.create().load("Mike Brock"))
            .finish()
            .publicMethod(String.class, "getName")
            .append(Stmt.create().loadVariable("name").returnValue())
            .finish().toJavaString();

    System.out.println(cls);

    assertEquals("package org.foo;\n" +
            "\n" +
            "public class Foo {\n" +
            "    private String name = \"Mike Brock\";\n" +
            "    public String getName() {\n" +
            "        return this.name;\n" +
            "    }\n" +
            "}", cls);
  }

  @Test
  public void testDefineClassB() {
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


