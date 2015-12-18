/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.stereotypes.client.test;

import org.jboss.errai.cdi.stereotypes.client.BorderCollie;
import org.jboss.errai.cdi.stereotypes.client.Chihuahua;
import org.jboss.errai.cdi.stereotypes.client.EnglishBorderCollie;
import org.jboss.errai.cdi.stereotypes.client.HighlandCow;
import org.jboss.errai.cdi.stereotypes.client.LongHairedDog;
import org.jboss.errai.cdi.stereotypes.client.MexicanChihuahua;
import org.jboss.errai.cdi.stereotypes.client.MiniatureClydesdale;
import org.jboss.errai.cdi.stereotypes.client.Moose;
import org.jboss.errai.cdi.stereotypes.client.Reindeer;
import org.jboss.errai.cdi.stereotypes.client.ShetlandPony;
import org.jboss.errai.cdi.stereotypes.client.Springbok;
import org.jboss.errai.cdi.stereotypes.client.Tame;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.SyncBeanDef;

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

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.stereotypes.StereotypesTestModule";
  }

  public void testStereotypeWithScopeType() {
    assertEquals(1, getBeans(Moose.class).size());
    assertEquals(ApplicationScoped.class, getBeans(Moose.class).iterator().next().getScope());
  }

  public void testStereotypeWithoutScopeType() {
    assertEquals(1, getBeans(Reindeer.class).size());
    assertEquals(Dependent.class, getBeans(Reindeer.class).iterator().next().getScope());
  }

  public void testOneStereotypeAllowed() {
    final SyncBeanDef<LongHairedDog> bean = getBeans(LongHairedDog.class).iterator().next();

    assertEquals(ApplicationScoped.class, bean.getScope());
  }

  private static final Tame TAME_LITERAL = new Tame() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Tame.class;
    }
  };

  public void testMultipleStereotypesAllowed() {
    assertEquals(1, getBeans(HighlandCow.class, TAME_LITERAL).size());

    final SyncBeanDef<HighlandCow> highlandCow = getBeans(HighlandCow.class, TAME_LITERAL).iterator().next();

    assertNull(highlandCow.getName());
    assertContains(highlandCow.getQualifiers(), TAME_LITERAL);
    assertEquals(ApplicationScoped.class, highlandCow.getScope());
  }

  public void testExplicitScopeOverridesMergedScopesFromMultipleStereotype() {
    assertEquals(1, getBeans(Springbok.class).size());
    assertEquals(Dependent.class, getBeans(Springbok.class).iterator().next().getScope());
  }

  public void testStereotypeDeclaredInheritedIsInherited() throws Exception {
    assertEquals(ApplicationScoped.class, getBeans(BorderCollie.class).iterator().next().getScope());
  }

  public void testStereotypeNotDeclaredInheritedIsNotInherited() {
    // NOTE: This is different form the TCK test in that, in Errai, we don't expect non-explicitly declared
    //       and un-reachable beans to be available from the bean manager. Thus, the correct behavior for us
    //       is that this bean is not registered at all.


    final Collection<SyncBeanDef<ShetlandPony>> beans = getBeans(ShetlandPony.class);

    // now that experimental support exists (testcase must support both modes)
    if (beans.size() > 1) {
      fail("should be none or one bean");
    }
  }

  public void testStereotypeDeclaredInheritedIsIndirectlyInherited() {
    assertEquals(ApplicationScoped.class, getBeans(EnglishBorderCollie.class).iterator().next().getScope());
  }

  public void testStereotypeNotDeclaredInheritedIsNotIndirectlyInherited() {
    // NOTE: This is different form the TCK test in that, in Errai, we don't expect non-explicitly declared
    //       and un-reachable beans to be available from the bean manager. Thus, the correct behavior for us
    //       is that this bean is not registered at all.
    final Collection<SyncBeanDef<MiniatureClydesdale>> beans = getBeans(MiniatureClydesdale.class);

    // now that experimental support exists (testcase must support both modes)
    if (beans.size() > 1) {
      fail("should be none or one bean");
    }
  }

  public void testStereotypeScopeIsOverriddenByInheritedScope() {
    assertEquals(Dependent.class, getBeans(Chihuahua.class).iterator().next().getScope());
  }

  public void testStereotypeScopeIsOverriddenByIndirectlyInheritedScope() {
    assertEquals(Dependent.class, getBeans(MexicanChihuahua.class).iterator().next().getScope());
  }
}
