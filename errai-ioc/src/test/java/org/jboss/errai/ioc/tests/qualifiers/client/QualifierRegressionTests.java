package org.jboss.errai.ioc.tests.qualifiers.client;

import java.lang.annotation.Annotation;

import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.tests.qualifiers.client.res.AImpl;
import org.jboss.errai.ioc.tests.qualifiers.client.res.ClassWIthArrayValuedQual;
import org.jboss.errai.ioc.tests.qualifiers.client.res.InjectionPoint;
import org.jboss.errai.ioc.tests.qualifiers.client.res.QualWithArrayValue;

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

  public void testQualifierEqualityWithArrayValue() throws Exception {
    // Test passes as long as this does not cause an error
    try {
      IOC.getBeanManager().lookupBean(ClassWIthArrayValuedQual.class, new QualWithArrayValue() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return QualWithArrayValue.class;
        }

        @Override
        public String[] value() {
          return new String[] { "test" };
        }
      }).getInstance();
    } catch (Throwable t) {
      throw new AssertionError("An error occured while looking up a bean with an array-valued qualifier.", t);
    }
  }

}
