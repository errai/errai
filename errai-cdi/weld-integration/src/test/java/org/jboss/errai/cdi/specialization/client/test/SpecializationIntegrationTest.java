package org.jboss.errai.cdi.specialization.client.test;

import org.jboss.errai.cdi.specialization.client.Expensive;
import org.jboss.errai.cdi.specialization.client.Landowner;
import org.jboss.errai.cdi.specialization.client.Lazy;
import org.jboss.errai.cdi.specialization.client.LazyFarmer;
import org.jboss.errai.cdi.specialization.client.Sparkly;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

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

  public void testSpecializingBeanHasQualifiersOfSpecializedAndSpecializingBean() {
    final Collection<IOCBeanDef> farmerBeans = IOC.getBeanManager().lookupBeans(LazyFarmer.class, LAZY_LITERAL);
    assertEquals("should only have one matching LazyFarmer bean", 1, farmerBeans.size());

    final IOCBeanDef<LazyFarmer> lazyFarmerBean = farmerBeans.iterator().next();
    final Set<Annotation> qualifiers = lazyFarmerBean.getQualifiers();

    assertTrue("wrong qualifiers: " + qualifiers, annotationSetMatches(qualifiers, Landowner.class, Lazy.class, Any.class, Named.class, Default.class));
  }

  public void testSpecializationBeanHasNameOfSpecializedBean() {
    final String expectedName = "farmer";
    final Collection<IOCBeanDef> beans = IOC.getBeanManager().lookupBeans(expectedName);

    assertEquals("should have one matching bean", 1, beans.size());

    final IOCBeanDef farmerBean = beans.iterator().next();
    assertEquals(expectedName, farmerBean.getName());
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

//  public void testSpecializingProducerMethod() {
//    final List<IOCBeanDef> expensiveNecklaceBeans
//        = IOC.getBeanManager().lookupBeans(Necklace.class, EXPENSIVE_LITERAL);
//
//    assertEquals(1, expensiveNecklaceBeans.size());
//  }
}
