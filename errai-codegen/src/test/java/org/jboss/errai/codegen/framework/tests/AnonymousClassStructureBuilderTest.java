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

import java.lang.annotation.Retention;

import org.jboss.errai.codegen.framework.Context;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.Variable;
import org.jboss.errai.codegen.framework.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.builder.impl.StatementBuilder;
import org.jboss.errai.codegen.framework.tests.model.Bar;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AnonymousClassStructureBuilderTest extends AbstractStatementBuilderTest {

  @Test
  public void testAnonymousAnnotation() {

    String src = ObjectBuilder.newInstanceOf(Retention.class)
            .extend()
            .publicOverridesMethod("annotationType")
            .append(Stmt.load(Retention.class).returnValue())
            .finish()
            .finish()
            .toJavaString();

    assertEquals("failed to generate anonymous annotation with overloaded method",
            "new java.lang.annotation.Retention() {\n" +
                    "public Class annotationType() {\n" +
                    "return java.lang.annotation.Retention.class;\n" +
                    "}\n" +
                    "}", src);
  }

  @Test
  public void testAnonymousClass() {

    String src = ObjectBuilder.newInstanceOf(Bar.class, Context.create().autoImport())
            .extend()
            .publicOverridesMethod("setName", Parameter.of(String.class, "name"))
            .append(Stmt.loadClassMember("name").assignValue(Variable.get("name")))
            .finish()
            .finish()
            .toJavaString();

    assertEquals("failed to generate anonymous class with overloaded construct",
            "new Bar() {\n" +
                    "public void setName(String name) {\n" +
                    "this.name = name;\n" +
                    "}\n" +
                    "}", src);
  }

  @Test
  public void testAnonymousClassWithInitializationBlock() {
    String src = ObjectBuilder.newInstanceOf(Bar.class, Context.create().autoImport())
            .extend()
            .initialize()
            .append(Stmt.loadClassMember("name").assignValue("init"))
            .finish()
            .publicOverridesMethod("setName", Parameter.of(String.class, "name"))
            .append(Stmt.loadClassMember("name").assignValue(Variable.get("name")))
            .finish()
            .finish()
            .toJavaString();

    assertEquals("failed to generate anonymous class with overloaded construct",
            "new Bar() {\n" +
                    "{\n" +
                    "name = \"init\";" +
                    "\n}\n" +
                    "public void setName(String name) {\n" +
                    "this.name = name;\n" +
                    "}\n" +
                    "}", src);
  }

  @Test
  public void testAnonymousClassReferencingOuterClass() {
    ClassStructureBuilder<?> outer = ClassBuilder.define("org.foo.Outer").publicScope().body();

    Statement anonInner = ObjectBuilder.newInstanceOf(Bar.class, Context.create().autoImport())
            .extend()
            .publicOverridesMethod("setName", Parameter.of(String.class, "name"))
            .append(Stmt.loadStatic(outer.getClassDefinition(), "this").loadField("outerName").assignValue(Variable.get("name")))
            .append(Stmt.loadStatic(outer.getClassDefinition(), "this").invoke("setOuterName", Variable.get("name")))
            .finish()
            .finish();

    String cls = outer
            .publicField("outerName", String.class)
            .finish()
            .publicMethod(void.class, "setOuterName", Parameter.of(String.class, "outerName"))
            .append(Stmt.loadClassMember("outerName").assignValue(Variable.get("outerName")))
            .finish()
            .publicMethod(void.class, "test")
            .append(anonInner)
            .finish()
            .toJavaString();

    assertEquals("failed to generate anonymous class accessing outer class",
            "package org.foo;\n" +

                    "import org.jboss.errai.codegen.framework.tests.model.Bar;\n" +
                    "import org.foo.Outer;\n" +

                    "public class Outer {\n" +
                    "public String outerName;\n" +
                    "public void setOuterName(String outerName) {\n" +
                    "this.outerName = outerName;\n" +
                    "}\n" +

                    "public void test() {\n" +
                    "new Bar() {\n" +
                    "public void setName(String name) {\n" +
                    "Outer.this.outerName = name;\n" +
                    "Outer.this.setOuterName(name);\n" +
                    "}\n" +
                    "};\n" +
                    "}\n" +
                    "}\n", cls);
  }

  @Test
  public void testAssignmentOfAnonymousClass() {
    Statement stmt = ObjectBuilder.newInstanceOf(Retention.class)
            .extend()
            .publicOverridesMethod("annotationType")
            .append(Stmt.load(Retention.class).returnValue())
            .finish()
            .finish();

    Statement declaration = Stmt.declareVariable(java.lang.annotation.Annotation.class)
            .named("foo").initializeWith(stmt);


    String cls = declaration.generate(Context.create());

    assertEquals("java.lang.annotation.Annotation foo = new java.lang.annotation.Retention() {\n" +
            "  public Class annotationType() {\n" +
            "    return java.lang.annotation.Retention.class;\n" +
            "  }\n" +
            "\n" +
            "\n" +
            "};", cls);
  }
}
