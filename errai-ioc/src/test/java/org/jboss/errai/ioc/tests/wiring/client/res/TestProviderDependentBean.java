package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class TestProviderDependentBean {
  @Inject TestProvidedIface testProvidedIface;

  public TestProvidedIface getTestProvidedIface() {
    return testProvidedIface;
  }
}
