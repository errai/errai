package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Singleton;

/**
 * This bean should never get returned as it should be overridden by an alternative for
 * AlternativeCommonInterfaceB
 * @author Mike Brock
 */
@Singleton
public class AltCommonInterfaceBImpl implements AlternativeCommonInterfaceB {
  @Override
  public void doSomethingElse() {
  }
}
