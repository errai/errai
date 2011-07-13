package org.jboss.errai.cdi.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class ProducerDependentTestBean {
  @Inject @A
  Integer aInteger;

  @Inject @B
  Integer bInteger;

  @Inject @C
  Integer cInteger;

  @Inject
  String producedString;

  public Integer getIntegerA() {
    return aInteger;
  }

  public Integer getIntegerB() {
    return bInteger;
  }

  public Integer getIntegerC() {
    return cInteger;
  }

  public String getProducedString() {
    return producedString;
  }
}