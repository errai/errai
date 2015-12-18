package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton @Alternative
public class OverridingAltCommonInterfaceBImpl implements AlternativeCommonInterfaceB {
  @Override
  public void doSomethingElse() {
  }
}
