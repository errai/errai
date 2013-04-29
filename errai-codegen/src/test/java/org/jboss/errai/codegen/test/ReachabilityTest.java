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

import com.google.gwt.thirdparty.guava.common.io.Files;
import org.jboss.errai.codegen.test.model.BeanWithTypeParmedMeths;
import org.jboss.errai.codegen.test.model.FakeBean;
import org.jboss.errai.codegen.util.QuickDeps;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ReachabilityTest {

  private static String getSource(Class clazz) throws Exception {
    final URL url = clazz.getClassLoader().getResource(clazz.getName().replaceAll("\\.", "/") + ".java");

    if (url == null) {
      throw new RuntimeException("not found!");
    }

    return Files.toString(new File(url.toURI()), Charset.forName("UTF-8"));
  }

  @Test
  public void testBasicReachability() throws Exception {
    final String source = getSource(BeanWithTypeParmedMeths.class);

    final Set<String> quickTypeDependencyList
        = QuickDeps.getQuickTypeDependencyList(source, BeanWithTypeParmedMeths.class.getClassLoader());

    final Set<String> expected = new HashSet<String>(
        Arrays.asList(
            "org.jboss.errai.codegen.test.model.BeanWithTypeParmedMeths",
            "org.jboss.errai.codegen.test.model.Foo",
            "org.jboss.errai.codegen.test.model.Bar",
            "java.util.Map"
        )
    );

    Assert.assertEquals(expected, quickTypeDependencyList);
  }

  @Test
  public void testBasicReachability2() throws Exception {
    final String source = getSource(FakeBean.class);

    final Set<String> quickTypeDependencyList
        = QuickDeps.getQuickTypeDependencyList(source, FakeBean.class.getClassLoader());

    System.out.println(quickTypeDependencyList);

    final Set<String> expected = new HashSet<String>(
        Arrays.asList(
            "org.jboss.errai.codegen.test.model.FakeBean",
            "javax.enterprise.inject.Instance",
            "javax.inject.Inject",
            "org.jboss.errai.codegen.test.model.Bar",
            "org.jboss.errai.codegen.test.model.Bwah",
            BigDecimal.class.getName(),
            BigInteger.class.getName(),
            String.class.getName(),
            Class.class.getName()
        )
    );

    Assert.assertEquals(expected, quickTypeDependencyList);
  }
}
