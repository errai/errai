package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@EntryPoint
public class HappyInspector {
  @Inject
  private HappyService happyService;

  public boolean confirmHappiness() {
    return happyService.isHappy();
  }

  @PostConstruct
  public void init() {
    INSTANCE = this;
  }

  public static HappyInspector INSTANCE;
}
