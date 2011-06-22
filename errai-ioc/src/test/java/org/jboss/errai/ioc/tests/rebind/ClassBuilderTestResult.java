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

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface ClassBuilderTestResult {

  public static final String CLASS_IMPLEMENTING_INTERFACE =
      "     package org.foo;" +
          "\n" +
          " import java.io.Serializable;\n" +
          "\n" +
          " public class Bar implements Serializable {\n" +
          "   private String name;\n" +
          " }";

  public static final String CLASS_WITH_ACCESSOR_METHODS =
      "     package org.foo;\n" +
          "\n" +
          " public class Foo {\n" +
          "   private String name = \"default\";\n" +
          "   public String getName() {\n" +
          "     return this.name;\n" +
          "   }\n" +
          "   public void setName(String name) {\n" +
          "     this.name = name;\n" +
          "   }\n" +
          " }";

  public static final String CLASS_WITH_PARENT =
      "     package org.foo;\n" +
          "\n" +
          " public class Foo extends String {\n" +
          "   public Foo(int i) {" +
          "   }" + 
          " }";
}