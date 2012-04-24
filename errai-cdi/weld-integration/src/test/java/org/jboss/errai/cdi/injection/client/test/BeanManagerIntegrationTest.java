package org.jboss.errai.cdi.injection.client.test;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import org.jboss.errai.cdi.injection.client.ApplicationScopedBean;
import org.jboss.errai.cdi.injection.client.CommonInterface;
import org.jboss.errai.cdi.injection.client.qualifier.LincolnBar;
import org.jboss.errai.cdi.injection.client.qualifier.QualA;
import org.jboss.errai.cdi.injection.client.qualifier.QualAppScopeBeanA;
import org.jboss.errai.cdi.injection.client.qualifier.QualAppScopeBeanB;
import org.jboss.errai.cdi.injection.client.qualifier.QualB;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.jboss.errai.ioc.client.container.IOCResolutionException;

/**
 * @author Mike Brock
 */
public class BeanManagerIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
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

  public void testBeanManagerAPIs() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        IOCBeanManager mgr = IOC.getBeanManager();

        IOCBeanDef<QualAppScopeBeanA> bean = mgr.lookupBean(QualAppScopeBeanA.class);

        final Set<Annotation> a = bean.getQualifiers();
        assertEquals("there should be one qualifier", 1, a.size());
        assertEquals("wrong qualifier", QualA.class, a.iterator().next().annotationType());

        assertEquals("unmanaged bean should have no entries", 0, mgr.lookupBeans(String.class).size());
        assertEquals("unmanaged bean should return null bean ref", null, mgr.lookupBean(String.class));

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

        final QualB qualB = new QualB() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualB.class;
          }
        };

        Collection<IOCBeanDef> beans = IOC.getBeanManager().lookupBeans(CommonInterface.class);
        assertEquals("wrong number of beans", 2, beans.size());

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

  public void testQualifiedLookupFailure() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final Annotation wrongAnno = new Annotation() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return LincolnBar.class;
          }
        };

        try {
          IOCBeanDef<CommonInterface> bean = IOC.getBeanManager().lookupBean(CommonInterface.class);
          fail("should have thrown an exception, but got: " + bean);
        }
        catch (IOCResolutionException e) {
          assertTrue("wrong exception thrown", e.getMessage().contains("multiple matching"));
        }

        try {
          IOCBeanDef<CommonInterface> bean = IOC.getBeanManager().lookupBean(CommonInterface.class, wrongAnno);
          fail("should have thrown an exception, but got: " + bean);
        }
        catch (IOCResolutionException e) {
          assertTrue("wrong exception thrown", e.getMessage().contains("no matching"));
        }

        finishTest();
      }
    });
  }
}