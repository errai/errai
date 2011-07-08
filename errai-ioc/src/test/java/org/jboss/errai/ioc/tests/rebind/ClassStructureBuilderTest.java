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

import java.lang.annotation.Retention;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.junit.Test;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ClassStructureBuilderTest extends AbstractStatementBuilderTest {
  
  @Test
  public void testOverrideConstructor() {

    String src = ObjectBuilder.newInstanceOf(Retention.class)
        .extend()
        .publicOverridesMethod("annotationType")
        .append(StatementBuilder.create().load("foo"))
        .append(StatementBuilder.create().load("bar"))
        .append(StatementBuilder.create().load("foobie"))
        .finish()
        .finish()
        .toJavaString();

    assertEquals("failed to generate anonymous class with overloaded construct", 
        "new java.lang.annotation.Retention() {\n" +
          "public Class annotationType() {\n" +
            "\"foo\";\n" +
            "\"bar\";\n" +
            "\"foobie\";\n" +
        "}\n" +
      "}", src);
  }
}
