package org.jboss.errai.cdi.integration.client.test;

import org.jboss.errai.cdi.integration.client.shared.ApplicationScopedBean;
import org.jboss.errai.cdi.integration.client.shared.CommonInterface;
import org.jboss.errai.cdi.integration.client.shared.QualA;
import org.jboss.errai.cdi.integration.client.shared.QualAppScopeBeanA;
import org.jboss.errai.cdi.integration.client.shared.QualAppScopeBeanB;
import org.jboss.errai.cdi.integration.client.shared.QualB;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public class BeanManagerIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTestModule";
  }

  public void testBeanManagerLookupSimple() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {

        IOCBeanDef<ApplicationScopedBean> bean = IOC.getBeanManager().lookupBean(ApplicationScopedBean.class);
        assertNotNull(bean);
        finishTest();
      }
    });
  }


  public void testQualifiedLookup() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final Annotation qualA = new Annotation() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualA.class;
          }
        };

        final Annotation qualB = new Annotation() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualB.class;
          }
        };

        IOCBeanDef<CommonInterface> beanA = IOC.getBeanManager().lookupBean(CommonInterface.class, qualA);
        assertNotNull("no bean found", beanA);
        assertTrue("wrong bean looked up", beanA.getInstance() instanceof QualAppScopeBeanA);


        IOCBeanDef<CommonInterface> beanB = IOC.getBeanManager().lookupBean(CommonInterface.class, qualB);
        assertNotNull("no bean found", beanB);
        assertTrue("wrong bean looked up", beanB.getInstance() instanceof QualAppScopeBeanB);
        finishTest();
      }
    });
  }

}
