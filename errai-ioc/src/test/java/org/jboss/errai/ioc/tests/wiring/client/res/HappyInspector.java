package org.jboss.errai.ioc.tests.wiring.client.res;

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

  @Inject
  private GenericService<Integer> integerService;

  @Inject
  private GenericService<String> stringService;
  
  @Inject
  private GenericServiceClassForLong longService;

  public HappyInspector() {
  }

  public boolean confirmHappiness() {
    return happyService.isHappy();
  }

  @PostConstruct
  public void init() {
    INSTANCE = this;
  }

  public GenericService<Integer> getIntegerService() {
    return integerService;
  }

  public GenericService<String> getStringService() {
    return stringService;
  }
  
  public GenericServiceClassForLong getLongService() {
    return longService;
  }

  public static HappyInspector INSTANCE;
}
