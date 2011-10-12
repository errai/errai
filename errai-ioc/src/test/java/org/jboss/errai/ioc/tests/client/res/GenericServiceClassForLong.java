package org.jboss.errai.ioc.tests.client.res;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class GenericServiceClassForLong extends GenericServiceClass<Long> {
  public Long get() {
    return 1l;
  }
}
