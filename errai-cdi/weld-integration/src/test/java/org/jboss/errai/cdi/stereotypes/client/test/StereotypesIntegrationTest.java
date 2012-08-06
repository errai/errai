package org.jboss.errai.cdi.stereotypes.client.test;

import org.jboss.errai.cdi.stereotypes.client.LongHairedDog;
import org.jboss.errai.cdi.stereotypes.client.Moose;
import org.jboss.errai.cdi.stereotypes.client.Reindeer;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOCBeanDef;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;

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
    final IOCBeanDef<LongHairedDog> bean = getBeans(LongHairedDog.class).iterator().next();

    assertEquals(ApplicationScoped.class, bean.getScope());
  }
}
