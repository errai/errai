package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@EntryPoint
public class HappyServiceImpl implements HappyService {
  @Override
  public boolean isHappy() {
    return true;
  }
}
