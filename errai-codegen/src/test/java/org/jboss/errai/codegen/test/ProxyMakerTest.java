/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.codegen.test;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.InnerClass;
import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.test.model.ToProxyBean;
import org.junit.Test;

/**
 * Tests for the {@link ProxyMaker}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ProxyMakerTest extends AbstractCodegenTest {

  private static final String expected =
      "public class ToProxy_Proxy extends org.jboss.errai.codegen.test.model.ToProxyBean {\n" +
          "  private org.jboss.errai.codegen.test.model.ToProxyBean $$_proxy_$$;\n" +
          "  public String getName() {\n" +
          "    return $$_proxy_$$.getName();\n" +
          "  }\n" +
          "  public org.jboss.errai.codegen.test.model.Integer getBlah() {\n" +
          "    return $$_proxy_$$.getBlah();\n" +
          "  }" +
          "  public String toString() {\n" +
          "    return $$_proxy_$$.toString();\n" +
          "  }\n" +
          "  public int hashCode() {\n" +
          "    if ($$_proxy_$$ == null) {\n" +
          "      throw new IllegalStateException(\"call to hashCode() on an unclosed proxy.\");\n" +
          "    } else {\n" +
          "      return $$_proxy_$$.hashCode();\n" +
          "    }\n" +
          "  }" +
          "  public boolean equals(Object o) {\n" +
          "    if ($$_proxy_$$ == null) {\n" +
          "      throw new IllegalStateException(\"call to equals() on an unclosed proxy.\");\n" +
          "    } else {\n" +
          "      return $$_proxy_$$.equals(o);\n" +
          "    }\n" +
          "  }\n" +
          "  public void __$setProxiedInstance$(org.jboss.errai.codegen.test.model.ToProxyBean proxy) {\n" +
          "    $$_proxy_$$ = proxy;\n" +
          "  }\n" +
          "}\n";

  @Test
  public void testProxyGeneration() {
    String proxy = new InnerClass(ProxyMaker.makeProxy("ToProxy_Proxy", ToProxyBean.class)).generate(Context.create());
    assertEquals("Faile to generated proxy", expected, proxy);
  }
}
