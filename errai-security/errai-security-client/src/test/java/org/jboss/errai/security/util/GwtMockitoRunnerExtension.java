package org.jboss.errai.security.util;

import java.util.Collection;

import org.junit.runners.model.InitializationError;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * Test runner allowing GwtMockito tests to run in maven with GWTTestCase tests.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class GwtMockitoRunnerExtension extends GwtMockitoTestRunner {

  public GwtMockitoRunnerExtension(Class<?> unitTestClass) throws InitializationError {
    super(unitTestClass);
  }

  @Override
  protected Collection<String> getPackagesToLoadViaStandardClassloader() {
    final Collection<String> retVal = super.getPackagesToLoadViaStandardClassloader();
    
    retVal.add("org.jboss.errai.mocksafe");
    retVal.add("com.google.gwtmockito");
    
    return retVal;
  }
}
