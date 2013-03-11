package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@IOCProvider
@Singleton
public class TestProvider implements Provider<TestProvidedIface> {
  @Override
  public TestProvidedIface get() {
    return new TestProvidedIface() {
      @Override
      public String getText() {
        return "foo";
      }
    };
  }
}
