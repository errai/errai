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

package org.jboss.errai.cdi.specialization.client.test;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Named;

import org.jboss.errai.cdi.specialization.client.Expensive;
import org.jboss.errai.cdi.specialization.client.Farmer;
import org.jboss.errai.cdi.specialization.client.Human;
import org.jboss.errai.cdi.specialization.client.Landowner;
import org.jboss.errai.cdi.specialization.client.Lazy;
import org.jboss.errai.cdi.specialization.client.LazyFarmer;
import org.jboss.errai.cdi.specialization.client.Necklace;
import org.jboss.errai.cdi.specialization.client.Product;
import org.jboss.errai.cdi.specialization.client.Sparkly;
import org.jboss.errai.cdi.specialization.client.Waste;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.IOCResolutionException;

/**
 * Tests for {@link javax.enterprise.inject.Specializes}
 *
 * @author Mike Brock
 */
public class SpecializationIntegrationTest extends AbstractErraiCDITest {
  {
    disableBus = false;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.specialization.SpecializationTestModule";
  }

  private static final Annotation LANDOWNER_LITERAL = new Landowner() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Landowner.class;
    }
  };

  private static final Annotation LAZY_LITERAL = new Lazy() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Lazy.class;
    }
  };

  public void testIndirectSpecialization() {
    // LazyFarmer specializes directly Farmer and indirectly Human
    final Collection<SyncBeanDef<Human>> humanBeans = getBeans(Human.class);
    assertEquals(1, humanBeans.size());
    final Collection<SyncBeanDef<Farmer>> farmerBeans = getBeans(Farmer.class, LANDOWNER_LITERAL);
    assertEquals(1, farmerBeans.size());
    final SyncBeanDef<Farmer> lazyFarmerBean = farmerBeans.iterator().next();
    assertEquals(lazyFarmerBean.getBeanClass(), humanBeans.iterator().next().getBeanClass());
  }

  public void testSpecializingBeanInjection1() {
    final SyncBeanDef<Farmer> farmerBean = IOC.getBeanManager().lookupBean(Farmer.class);
    assertNotNull(farmerBean);

    final Farmer farmer = farmerBean.getInstance();
    assertNotNull(farmer);

    assertEquals(farmer.getClassName(), LazyFarmer.class.getName());
  }

  public void testSpecializingBeanHasQualifiersOfSpecializedAndSpecializingBean() {
    final Collection<SyncBeanDef<LazyFarmer>> farmerBeans
        = IOC.getBeanManager().lookupBeans(LazyFarmer.class, LAZY_LITERAL);
    assertEquals("should only have one matching LazyFarmer bean", 1, farmerBeans.size());

    final SyncBeanDef<LazyFarmer> lazyFarmerBean = farmerBeans.iterator().next();
    final Set<Annotation> qualifiers = lazyFarmerBean.getQualifiers();

    assertTrue("wrong qualifiers: " + qualifiers,
        annotationSetMatches(qualifiers, Landowner.class, Lazy.class, Any.class, Named.class, Default.class));
  }

  public void testSpecializationBeanHasNameOfSpecializedBean() {
    final String expectedName = "farmer";
    final Collection<SyncBeanDef> beans = IOC.getBeanManager().lookupBeans(expectedName);

    assertEquals("should have one matching bean, but got: " + beans, 1, beans.size());

    final SyncBeanDef farmerBean = beans.iterator().next();
    assertEquals(expectedName, farmerBean.getName());
  }

  public void testProducerMethodOnSpecializedBeanNotCalled() {
    assertEquals(0, IOC.getBeanManager().lookupBeans(Waste.class).size());

    try {
      IOC.getBeanManager().lookupBean(Waste.class);
    }
    catch (IOCResolutionException e) {
      return;
    }

    fail("should have thrown lookup exception");
  }

  private static final Annotation EXPENSIVE_LITERAL = new Expensive() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Expensive.class;
    }
  };

  private static final Annotation SPARKLY_LITERAL = new Sparkly() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Sparkly.class;
    }
  };

  public void testSpecializingProducerMethod() {
    final Collection<SyncBeanDef<Necklace>> expensiveNecklaceBeans
        = IOC.getBeanManager().lookupBeans(Necklace.class, EXPENSIVE_LITERAL);

    assertEquals(1, expensiveNecklaceBeans.size());

    final SyncBeanDef<Necklace> expensiveNecklaceBean = expensiveNecklaceBeans.iterator().next();

    final Set<Annotation> expensiveNecklaceQualifiers = expensiveNecklaceBean.getQualifiers();
    assertEquals(4, expensiveNecklaceQualifiers.size());
    assertTrue(annotationSetMatches(expensiveNecklaceQualifiers, Expensive.class, Sparkly.class, Any.class, Named.class));

    final Collection<SyncBeanDef<Necklace>> sparklyNecklaceBeans
        = IOC.getBeanManager().lookupBeans(Necklace.class, SPARKLY_LITERAL);
    assertEquals(1, sparklyNecklaceBeans.size());
    final SyncBeanDef<Necklace> sparklyBean = sparklyNecklaceBeans.iterator().next();
    assertEquals("expensiveGift", sparklyBean.getName());
  }

  public void testSpecializingBeanInjection2() {
    final SyncBeanDef<Product> productBean = IOC.getBeanManager().lookupBean(Product.class, EXPENSIVE_LITERAL);
    assertNotNull(productBean);

    final Product product = productBean.getInstance();

    assertTrue(product instanceof Necklace);
    assertEquals(product.getPrice(), 10);
  }
}
