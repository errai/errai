package org.jboss.errai;

import java.util.Collection;

import org.junit.runners.model.InitializationError;

import com.google.gwtmockito.GwtMockitoTestRunner;

/**
 * Test runner allowing GwtMockito tests to run in maven with GWTTestCase tests.
 *
 */
public class GwtMockitoRunnerExtension extends GwtMockitoTestRunner {

  public GwtMockitoRunnerExtension(Class<?> unitTestClass) throws InitializationError {
    super(unitTestClass);
  }

  @Override
  protected Collection<String> getPackagesToLoadViaStandardClassloader() {
    final Collection<String> blacklisted = super.getPackagesToLoadViaStandardClassloader();
    
    blacklisted.add("com.google.gwtmockito");

    return blacklisted;
  }
}
