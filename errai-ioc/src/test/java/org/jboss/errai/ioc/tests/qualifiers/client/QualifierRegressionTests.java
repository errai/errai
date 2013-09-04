package org.jboss.errai.ioc.tests.qualifiers.client;

import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.tests.qualifiers.client.res.AImpl;
import org.jboss.errai.ioc.tests.qualifiers.client.res.InjectionPoint;

public class QualifierRegressionTests extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.qualifiers.QualifierRegressionTests";
  }
  
  /**
   * Regression test for ERRAI-340
   */
  public void testInjectWithUnqualifiedImpl() throws Exception {
    InjectionPoint injectionPoint = IOC.getBeanManager().lookupBean(InjectionPoint.class).getInstance();
    
    assertTrue("Injected value should be instance of AImpl", injectionPoint.getInjected() instanceof AImpl);
  }

}
