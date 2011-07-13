package org.jboss.errai.cdi.client;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock <cbrock@redhat.com>
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

  public Integer getaInteger() {
    return aInteger;
  }

  public Integer getbInteger() {
    return bInteger;
  }

  public Integer getcInteger() {
    return cInteger;
  }

  public String getProducedString() {
    return producedString;
  }
}
