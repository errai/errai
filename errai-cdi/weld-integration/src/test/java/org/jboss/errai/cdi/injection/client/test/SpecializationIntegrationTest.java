package org.jboss.errai.cdi.injection.client.test;

import org.jboss.errai.cdi.injection.client.Landowner;
import org.jboss.errai.cdi.injection.client.Lazy;
import org.jboss.errai.cdi.injection.client.LazyFarmer;
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
 * @author Mike Brock
 */
public class SpecializationIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  private static Annotation LANDOWNER_LITERAL = new Landowner() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Landowner.class;
    }
  };

  private static Annotation LAZY_LITERAL = new Lazy() {
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

    assertTrue(annotationSetMatches(qualifiers, Landowner.class, Lazy.class, Any.class, Named.class, Default.class));
  }

  public void testSpecializationBeanHasNameOfSpecializedBean() {
    final String expectedName = "farmer";
    final Collection<IOCBeanDef> beans = IOC.getBeanManager().lookupBeans(expectedName);

    assertEquals("should have one matching bean", 1, beans.size());

    final IOCBeanDef farmerBean = beans.iterator().next();
    assertEquals(expectedName, farmerBean.getName());
  }
}
