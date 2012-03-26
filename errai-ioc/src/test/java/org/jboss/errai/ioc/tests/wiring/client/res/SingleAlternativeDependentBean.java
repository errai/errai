package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class SingleAlternativeDependentBean {
  @Inject AlternativeCommonInterfaceB alternativeCommonInterfaceB;

  public AlternativeCommonInterfaceB getAlternativeCommonInterfaceB() {
    return alternativeCommonInterfaceB;
  }
}
