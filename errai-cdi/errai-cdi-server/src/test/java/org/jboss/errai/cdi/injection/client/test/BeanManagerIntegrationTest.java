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

package org.jboss.errai.cdi.injection.client.test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;

import org.jboss.errai.cdi.injection.client.AbstractBean;
import org.jboss.errai.cdi.injection.client.ApplicationScopedBean;
import org.jboss.errai.cdi.injection.client.CommonInterface;
import org.jboss.errai.cdi.injection.client.CommonInterfaceB;
import org.jboss.errai.cdi.injection.client.Cow;
import org.jboss.errai.cdi.injection.client.CreditCard;
import org.jboss.errai.cdi.injection.client.DependentScopedBean;
import org.jboss.errai.cdi.injection.client.DependentScopedBeanWithDependencies;
import org.jboss.errai.cdi.injection.client.DisabledAlternativeBean;
import org.jboss.errai.cdi.injection.client.FoobieScopedBean;
import org.jboss.errai.cdi.injection.client.FoobieScopedOverriddenBean;
import org.jboss.errai.cdi.injection.client.InheritedApplicationScopedBean;
import org.jboss.errai.cdi.injection.client.InheritedFromAbstractBean;
import org.jboss.errai.cdi.injection.client.InterfaceA;
import org.jboss.errai.cdi.injection.client.InterfaceB;
import org.jboss.errai.cdi.injection.client.InterfaceC;
import org.jboss.errai.cdi.injection.client.InterfaceD;
import org.jboss.errai.cdi.injection.client.InterfaceRoot;
import org.jboss.errai.cdi.injection.client.InterfaceWithNamedImpls;
import org.jboss.errai.cdi.injection.client.OuterBeanInterface;
import org.jboss.errai.cdi.injection.client.Pig;
import org.jboss.errai.cdi.injection.client.Visa;
import org.jboss.errai.cdi.injection.client.qualifier.LincolnBar;
import org.jboss.errai.cdi.injection.client.qualifier.QualA;
import org.jboss.errai.cdi.injection.client.qualifier.QualAppScopeBeanA;
import org.jboss.errai.cdi.injection.client.qualifier.QualAppScopeBeanB;
import org.jboss.errai.cdi.injection.client.qualifier.QualB;
import org.jboss.errai.cdi.injection.client.qualifier.QualEnum;
import org.jboss.errai.cdi.injection.client.qualifier.QualParmAppScopeBeanApples;
import org.jboss.errai.cdi.injection.client.qualifier.QualParmAppScopeBeanOranges;
import org.jboss.errai.cdi.injection.client.qualifier.QualV;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BeanManagerIntegrationTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  private final QualA QUAL_A = new QualA() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return QualA.class;
    }
  };

  private Any anyAnno = new Any() {

    @Override
    public Class<? extends Annotation> annotationType() {
      return Any.class;
    }
  };

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testBeanManagerLookupSimple() {
    final SyncBeanDef<ApplicationScopedBean> bean = IOC.getBeanManager().lookupBean(ApplicationScopedBean.class);
    assertNotNull(bean);
  }

  public void testBeanManagerLookupInheritedScopeBean() {
    final SyncBeanDef<InheritedApplicationScopedBean> bean =
        IOC.getBeanManager().lookupBean(InheritedApplicationScopedBean.class, anyAnno);
    assertNotNull("inherited application scoped bean did not lookup", bean);

    final InheritedApplicationScopedBean beanInst = bean.getInstance();
    assertNotNull("bean instance is null", beanInst);

    final DependentScopedBean bean1 = beanInst.getBean1();
    assertNotNull("bean1 is null", bean1);

    final DependentScopedBeanWithDependencies beanWithDependencies = beanInst.getBeanWithDependencies();
    assertNotNull("beanWithDependencies is null", beanWithDependencies);

    final DependentScopedBean bean2 = beanWithDependencies.getBean();
    assertNotSame("bean1 and bean2 should be different", bean1, bean2);

    final InheritedApplicationScopedBean beanInst2 = bean.getInstance();
    assertSame("bean is not observing application scope", beanInst, beanInst2);
  }

  public void testBeanManagerLookupBeanFromAbstractRootType() {
    final SyncBeanDef<AbstractBean> bean = IOC.getBeanManager().lookupBean(AbstractBean.class);
    assertNotNull("did not find any beans matching", bean);

    final AbstractBean beanInst = bean.getInstance();
    assertNotNull("bean instance is null", beanInst);

    assertTrue("bean is incorrect instance: " + beanInst.getClass(), beanInst instanceof InheritedFromAbstractBean);
  }

  /**
   * This test effectively tests that the IOC container comprehends the full type hierarchy, considering both supertypes
   * and transverse interface types.
   */
  public void testBeanManagerLookupForOuterInterfaceRootType() {
    final SyncBeanDef<OuterBeanInterface> bean = IOC.getBeanManager().lookupBean(OuterBeanInterface.class);
    assertNotNull("did not find any beans matching", bean);

    final OuterBeanInterface beanInst = bean.getInstance();
    assertNotNull("bean instance is null", beanInst);

    assertTrue("bean is incorrect instance: " + beanInst.getClass(), beanInst instanceof InheritedFromAbstractBean);
  }

  public void testBeanManagerLookupForOuterInterfacesOfNonAbstractType() {
    final SyncBeanDef<InterfaceC> beanC = IOC.getBeanManager().lookupBean(InterfaceC.class);
    assertNotNull("did not find any beans matching", beanC);

    final SyncBeanDef<InterfaceD> beanD = IOC.getBeanManager().lookupBean(InterfaceD.class);
    assertNotNull("did not find any beans matching", beanD);
  }

  public void testBeanManagerLookupForExtendedInterfaceType() {
    // This should find ApplicationScopedBeanA, ApplicationScopedBeanB and ApplicationScopedBeanC
    final Collection<SyncBeanDef<InterfaceRoot>> beans = IOC.getBeanManager().lookupBeans(InterfaceRoot.class);
    assertEquals("did not find all managed implementations of " + InterfaceRoot.class.getName(), 3, beans.size());

    // This should find ApplicationScopedBeanA and ApplicationScopedBeanB (InterfaceB extends InterfaceA)
    final Collection<SyncBeanDef<InterfaceA>> beansB = IOC.getBeanManager().lookupBeans(InterfaceA.class);
    assertEquals("did not find both managed implementations of " + InterfaceA.class.getName(), 2, beansB.size());

    // This should find only ApplicationScopedBeanB
    final Collection<SyncBeanDef<InterfaceB>> beansC = IOC.getBeanManager().lookupBeans(InterfaceB.class);
    assertEquals("did not find exactly one managed implementation of " + InterfaceB.class.getName(), 1, beansC.size());
  }

  public void testBeanManagerAPIs() {
    final SyncBeanManager mgr = IOC.getBeanManager();
    final SyncBeanDef<QualAppScopeBeanA> bean = mgr.lookupBean(QualAppScopeBeanA.class, anyAnno);

    final Set<Annotation> a = bean.getQualifiers();
    assertEquals("there should be two qualifiers", 2, a.size());
    assertTrue("wrong qualifiers", annotationSetMatches(a, QualA.class, Any.class));
  }

  public void testQualifiedLookup() {
    final QualA qualA = new QualA() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return QualA.class;
      }
    };

    final QualB qualB = new QualB() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return QualB.class;
      }
    };

    final Collection<SyncBeanDef<CommonInterface>> beans = IOC.getBeanManager().lookupBeans(CommonInterface.class);
    assertEquals("wrong number of beans", 2, beans.size());

    final SyncBeanDef<CommonInterface> beanA = IOC.getBeanManager().lookupBean(CommonInterface.class, qualA);
    assertNotNull("no bean found", beanA);
    assertTrue("wrong bean looked up", beanA.getInstance() instanceof QualAppScopeBeanA);

    final SyncBeanDef<CommonInterface> beanB = IOC.getBeanManager().lookupBean(CommonInterface.class, qualB);
    assertNotNull("no bean found", beanB);
    assertTrue("wrong bean looked up", beanB.getInstance() instanceof QualAppScopeBeanB);
  }

  public void testQualifierLookupWithAnnoAttrib() {
    final QualV qualApples = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.APPLES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 5;
      }
    };

    final QualV qualOranges = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.ORANGES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 6;
      }
    };

    final Collection<SyncBeanDef<CommonInterfaceB>> beans = IOC.getBeanManager().lookupBeans(CommonInterfaceB.class);
    assertEquals("wrong number of beans", 2, beans.size());

    final SyncBeanDef<CommonInterfaceB> beanA = IOC.getBeanManager().lookupBean(CommonInterfaceB.class, qualApples);
    assertNotNull("no bean found", beanA);
    assertTrue("wrong bean looked up", beanA.getInstance() instanceof QualParmAppScopeBeanApples);

    final SyncBeanDef<CommonInterfaceB> beanB = IOC.getBeanManager().lookupBean(CommonInterfaceB.class, qualOranges);
    assertNotNull("no bean found", beanB);
    assertTrue("wrong bean looked up", beanB.getInstance() instanceof QualParmAppScopeBeanOranges);
  }

  public void testQualifierLookupWithAnnoAttribFailure() {
    final QualV qualOrange = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.ORANGES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 5;
      }
    };

    final QualV qualApple = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.APPLES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 6;
      }
    };

    final Collection<SyncBeanDef<CommonInterfaceB>> beans = IOC.getBeanManager().lookupBeans(CommonInterfaceB.class);
    assertEquals("wrong number of beans", 2, beans.size());

    final SyncBeanDef<CommonInterfaceB> beanA = IOC.getBeanManager().lookupBean(CommonInterfaceB.class, qualOrange);
    assertNotNull("no bean found", beanA);
    assertFalse("wrong bean looked up", beanA.getInstance() instanceof QualParmAppScopeBeanApples);

    final SyncBeanDef<CommonInterfaceB> beanB = IOC.getBeanManager().lookupBean(CommonInterfaceB.class, qualApple);
    assertNotNull("no bean found", beanB);
    assertFalse("wrong bean looked up", beanB.getInstance() instanceof QualParmAppScopeBeanOranges);
  }

  public void testQualifiedLookupFailure() {
    final LincolnBar wrongAnno = new LincolnBar() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return LincolnBar.class;
      }
    };

    try {
      final SyncBeanDef<CommonInterface> bean = IOC.getBeanManager().lookupBean(CommonInterface.class, anyAnno);
      fail("should have thrown an exception, but got: " + bean);
    }
    catch (IOCResolutionException e) {
      assertTrue("wrong exception thrown: " + e.getMessage(), e.getMessage().contains("Multiple beans matched"));
    }

    try {
      final SyncBeanDef<CommonInterface> bean = IOC.getBeanManager().lookupBean(CommonInterface.class, wrongAnno);
      fail("should have thrown an exception, but got: " + bean);
    }
    catch (IOCResolutionException e) {
      assertTrue("wrong exception thrown: " + e.getMessage(), e.getMessage().contains("No beans matched"));
    }
  }

  public void testLookupByName() {
    final Collection<SyncBeanDef> beans = IOC.getBeanManager().lookupBeans("animal");

    assertEquals("wrong number of beans", 2, beans.size());
    assertTrue("should contain a pig", containsInstanceOf(beans, Pig.class));
    assertTrue("should contain a cow", containsInstanceOf(beans, Cow.class));

    for (SyncBeanDef<?> bean : beans) {
      assertEquals("animal", bean.getName());
    }
  }

  public void testNameAvailableThroughInterfaceLookup() {
    Collection<SyncBeanDef<CreditCard>> beans = IOC.getBeanManager().lookupBeans(CreditCard.class);
    for (SyncBeanDef<CreditCard> bean : beans) {
      if (bean.getBeanClass().getName().endsWith("Visa")) {
        assertEquals("visa", bean.getName());
      }
      else if (bean.getBeanClass().getName().endsWith("Amex")) {
        assertEquals("amex", bean.getName());
      }
      else {
        fail("Unexpected bean was returned from lookup: " + bean);
      }
    }
  }

  public void testNameAvailableThroughConcreteTypeLookup() {
    Collection<SyncBeanDef<Visa>> beans = IOC.getBeanManager().lookupBeans(Visa.class);
    for (SyncBeanDef<Visa> bean : beans) {
      assertNotNull("Missing name on " + bean, bean.getName());
    }
  }

  public void testLookupAllBeans() {
    final Collection<SyncBeanDef<Object>> beans = IOC.getBeanManager().lookupBeans(Object.class);

    assertTrue(!beans.isEmpty());
  }

  public void testLookupAllBeansQualified() {
    final Collection<SyncBeanDef<Object>> beans = IOC.getBeanManager().lookupBeans(Object.class, QUAL_A);

    assertEquals(1, beans.size());
    assertEquals(QualAppScopeBeanA.class, beans.iterator().next().getBeanClass());
  }

  public void testReportedScopeCorrect() {
    final SyncBeanDef<ApplicationScopedBean> appScopeBean = IOC.getBeanManager().lookupBean(ApplicationScopedBean.class);
    final SyncBeanDef<DependentScopedBean> dependentIOCBean = IOC.getBeanManager().lookupBean(DependentScopedBean.class);

    assertEquals(ApplicationScoped.class, appScopeBean.getScope());
    assertEquals(Dependent.class, dependentIOCBean.getScope());
  }

  public void testAddingProgrammaticDestructionCallback() {
    final DependentScopedBean dependentScopedBean
        = IOC.getBeanManager().lookupBean(DependentScopedBean.class).newInstance();

    class TestValueHolder {
      boolean destroyed = false;
    }

    final TestValueHolder testValueHolder = new TestValueHolder();

    IOC.getBeanManager().addDestructionCallback(dependentScopedBean, new DestructionCallback<Object>() {
      @Override
      public void destroy(Object bean) {
        testValueHolder.destroyed = true;
      }
    });

    IOC.getBeanManager().destroyBean(dependentScopedBean);

    assertEquals(true, testValueHolder.destroyed);
  }

  /**
   * Tests that beans marked as Dependent scoped by an IOCExtension can still be forced into a different scope (in this
   * case, ApplicationScoped) when they are annotated as such.
   * <p>
   * Besides this being a good idea on its own, both Errai UI Templates and Errai Navigation rely on this behaviour.
   */
  public void testNormalScopeOverridesDependent() {
    final FoobieScopedBean foobieScopedBean1 = IOC.getBeanManager().lookupBean(FoobieScopedBean.class).getInstance();
    final FoobieScopedBean foobieScopedBean2 = IOC.getBeanManager().lookupBean(FoobieScopedBean.class).getInstance();

    assertNotNull(foobieScopedBean1);
    assertNotSame(foobieScopedBean1, foobieScopedBean2);

    final FoobieScopedOverriddenBean foobieScopedOverriddenBean1
        = IOC.getBeanManager().lookupBean(FoobieScopedOverriddenBean.class).getInstance();

    final FoobieScopedOverriddenBean foobieScopedOverriddenBean2
        = IOC.getBeanManager().lookupBean(FoobieScopedOverriddenBean.class).getInstance();

    assertNotNull(foobieScopedOverriddenBean1);
    assertSame(foobieScopedOverriddenBean1, foobieScopedOverriddenBean2);
  }

  public void testProgrammaticlyRegisteredBeansAreLookedUp() {
    final SyncBeanManager bm = IOC.getBeanManager();
    assertEquals("The disabled alternative must not be in the bean manager before being programmatically added.", 0,
            bm.lookupBeans(DisabledAlternativeBean.class).size());

    bm.registerBean(new SyncBeanDef<DisabledAlternativeBean>() {

      @Override
      public Class<DisabledAlternativeBean> getType() {
        return DisabledAlternativeBean.class;
      }

      @Override
      public Class<?> getBeanClass() {
        return DisabledAlternativeBean.class;
      }

      @Override
      public Class<? extends Annotation> getScope() {
        return Dependent.class;
      }

      @Override
      public DisabledAlternativeBean getInstance() {
        return new DisabledAlternativeBean();
      }

      @Override
      public DisabledAlternativeBean newInstance() {
        return new DisabledAlternativeBean();
      }

      @Override
      public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
      }

      @Override
      public boolean matches(Set<Annotation> annotations) {
        return true;
      }

      @Override
      public String getName() {
        return "Name of DisabledAlternative";
      }

      @Override
      public boolean isActivated() {
        return true;
      }

      @Override
      public boolean isAssignableTo(Class<?> type) {
        return Arrays.asList(Object.class, DisabledAlternativeBean.class).contains(type);
      }
    });

    assertEquals("Failed to lookup programmatically added bean by type.", 1, bm.lookupBeans(DisabledAlternativeBean.class).size());
    assertEquals("Failed to lookup programmatically added bean by name.", 1, bm.lookupBeans("Name of DisabledAlternative").size());
  }

  public void testLookupByNameDoesNotFindOtherBeansOfSameType() throws Exception {
    final SyncBeanManager beanManager = IOC.getBeanManager();
    beanManager.registerBean(new SyncBeanDef<InterfaceWithNamedImpls>() {

      @Override
      public Class<InterfaceWithNamedImpls> getType() {
        return InterfaceWithNamedImpls.class;
      }

      @Override
      public Class<?> getBeanClass() {
        return InterfaceWithNamedImpls.class;
      }

      @Override
      public Class<? extends Annotation> getScope() {
        return Dependent.class;
      }

      @Override
      public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
      }

      @Override
      public boolean matches(Set<Annotation> annotations) {
        return true;
      }

      @Override
      public String getName() {
        return "Programmatic";
      }

      @Override
      public boolean isActivated() {
        return true;
      }

      @Override
      public InterfaceWithNamedImpls getInstance() {
        return null;
      }

      @Override
      public InterfaceWithNamedImpls newInstance() {
        return null;
      }

      @Override
      public boolean isAssignableTo(Class<?> type) {
        return Arrays.asList(Object.class, InterfaceWithNamedImpls.class).contains(type);
      }
    });

    assertEquals("Found multiple beans looking up @Named bean.", 1, beanManager.lookupBeans("Named").size());
    assertEquals("Found multiple beans looking up programmatically registered bean by name.", 1,
            beanManager.lookupBeans("Programmatic").size());
  }

  private static boolean containsInstanceOf(final Collection<SyncBeanDef> defs, final Class<?> clazz) {
    for (final SyncBeanDef def : defs) {
      if (def.getType().equals(clazz)) return true;
    }
    return false;
  }
}
