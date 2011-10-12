package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.enterprise.context.ApplicationScoped;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class GenericServiceClassForLong extends GenericServiceClass<Long> {
  public Long get() {
    return 1l;
  }
}