package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@EntryPoint
public class HappyInspector {
  @Inject
  private HappyService happyService;

  @Inject @Any
  private GenericService<Integer> integerService;

  @Inject @Any
  private GenericService<String> stringService;
  
  @Inject
  private GenericServiceClassForLong longService;

  public HappyInspector() {
  }

  public boolean confirmHappiness() {
    return happyService.isHappy();
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
}
