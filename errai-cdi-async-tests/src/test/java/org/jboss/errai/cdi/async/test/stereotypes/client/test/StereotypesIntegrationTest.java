/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.cdi.async.test.stereotypes.client.test;


import org.jboss.errai.cdi.async.test.stereotypes.client.BorderCollie;
import org.jboss.errai.cdi.async.test.stereotypes.client.Chihuahua;
import org.jboss.errai.cdi.async.test.stereotypes.client.EnglishBorderCollie;
import org.jboss.errai.cdi.async.test.stereotypes.client.HighlandCow;
import org.jboss.errai.cdi.async.test.stereotypes.client.LongHairedDog;
import org.jboss.errai.cdi.async.test.stereotypes.client.MexicanChihuahua;
import org.jboss.errai.cdi.async.test.stereotypes.client.MiniatureClydesdale;
import org.jboss.errai.cdi.async.test.stereotypes.client.Moose;
import org.jboss.errai.cdi.async.test.stereotypes.client.Reindeer;
import org.jboss.errai.cdi.async.test.stereotypes.client.ShetlandPony;
import org.jboss.errai.cdi.async.test.stereotypes.client.Springbok;
import org.jboss.errai.cdi.async.test.stereotypes.client.Tame;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * @author Mike Brock
 */
public class StereotypesIntegrationTest extends AbstractErraiCDITest {
  {
    disableBus = false;
  }

  protected <T> Collection<AsyncBeanDef<T>> getAsyncBeans(final Class<T> type,
                                                   final Annotation... annotations) {
    return IOC.getAsyncBeanManager().lookupBeans(type, annotations);
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.stereotypes.AsyncStereotypesTestModule";
  }

  public void testStereotypeWithScopeType() {
    assertEquals(1, getAsyncBeans(Moose.class).size());
    assertEquals(ApplicationScoped.class, getAsyncBeans(Moose.class).iterator().next().getScope());
  }

  public void testStereotypeWithoutScopeType() {
    assertEquals(1, getAsyncBeans(Reindeer.class).size());
    assertEquals(Dependent.class, getAsyncBeans(Reindeer.class).iterator().next().getScope());
  }

  public void testOneStereotypeAllowed() {
    final AsyncBeanDef<LongHairedDog> bean = getAsyncBeans(LongHairedDog.class).iterator().next();

    assertEquals(ApplicationScoped.class, bean.getScope());
  }

  private static final Tame TAME_LITERAL = new Tame() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Tame.class;
    }
  };

  public void testMultipleStereotypesAllowed() {
    assertEquals(1, getAsyncBeans(HighlandCow.class, TAME_LITERAL).size());

    final AsyncBeanDef<HighlandCow> highlandCow = getAsyncBeans(HighlandCow.class, TAME_LITERAL).iterator().next();

    assertNull(highlandCow.getName());
    assertContains(highlandCow.getQualifiers(), TAME_LITERAL);
    assertEquals(ApplicationScoped.class, highlandCow.getScope());
  }

  public void testExplicitScopeOverridesMergedScopesFromMultipleStereotype() {
    assertEquals(1, getAsyncBeans(Springbok.class).size());
    assertEquals(Dependent.class, getAsyncBeans(Springbok.class).iterator().next().getScope());
  }

  public void testStereotypeDeclaredInheritedIsInherited() throws Exception {
    assertEquals(ApplicationScoped.class, getAsyncBeans(BorderCollie.class).iterator().next().getScope());
  }

  public void testStereotypeNotDeclaredInheritedIsNotInherited() {
    // NOTE: This is different form the TCK test in that, in Errai, we don't expect non-explicitly declared
    //       and un-reachable beans to be available from the bean manager. Thus, the correct behavior for us
    //       is that this bean is not registered at all.


    final Collection<AsyncBeanDef<ShetlandPony>> beans = getAsyncBeans(ShetlandPony.class);

    // now that experimental support exists (testcase must support both modes)
    if (beans.size() > 1) {
      fail("should be none or one bean");
    }
  }

  public void testStereotypeDeclaredInheritedIsIndirectlyInherited() {
    assertEquals(ApplicationScoped.class, getAsyncBeans(EnglishBorderCollie.class).iterator().next().getScope());
  }

  public void testStereotypeNotDeclaredInheritedIsNotIndirectlyInherited() {
    // NOTE: This is different form the TCK test in that, in Errai, we don't expect non-explicitly declared
    //       and un-reachable beans to be available from the bean manager. Thus, the correct behavior for us
    //       is that this bean is not registered at all.
    final Collection<AsyncBeanDef<MiniatureClydesdale>> beans = getAsyncBeans(MiniatureClydesdale.class);

    // now that experimental support exists (testcase must support both modes)
    if (beans.size() > 1) {
      fail("should be none or one bean");
    }
  }

  public void testStereotypeScopeIsOverriddenByInheritedScope() {
    assertEquals(Dependent.class, getAsyncBeans(Chihuahua.class).iterator().next().getScope());
  }

  public void testStereotypeScopeIsOverriddenByIndirectlyInheritedScope() {
    assertEquals(Dependent.class, getAsyncBeans(MexicanChihuahua.class).iterator().next().getScope());
  }
}
