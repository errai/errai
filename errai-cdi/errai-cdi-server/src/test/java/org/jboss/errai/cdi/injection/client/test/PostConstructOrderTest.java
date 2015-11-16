package org.jboss.errai.cdi.injection.client.test;

import org.jboss.errai.cdi.injection.client.PostConstrBeanA;
import org.jboss.errai.cdi.injection.client.PostConstrBeanB;
import org.jboss.errai.cdi.injection.client.PostConstrBeanC;
import org.jboss.errai.cdi.injection.client.PostConstructTestUtil;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.List;

/**
 * @author Mike Brock
 */
public class PostConstructOrderTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testPostConstructFiresInCorrectOrderR() {
    PostConstructTestUtil.reset();
    final PostConstrBeanA beanA = IOC.getBeanManager().lookupBean(PostConstrBeanA.class).getInstance();

    assertNotNull("PostConstrBeanA was not resolved", beanA);

    final List<String> postConstructOrder = PostConstructTestUtil.getOrderOfFiring();

    assertEquals(PostConstrBeanC.class.getName(), postConstructOrder.get(0));
    assertEquals(PostConstrBeanB.class.getName(), postConstructOrder.get(1));
    assertEquals(PostConstrBeanA.class.getName(), postConstructOrder.get(2));
  }
}
