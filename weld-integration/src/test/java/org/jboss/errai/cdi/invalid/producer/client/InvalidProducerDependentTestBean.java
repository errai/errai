package org.jboss.errai.cdi.invalid.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class InvalidProducerDependentTestBean {
  // cannot be satisfied (no producer for @A @B available)
  @Inject @A @B
  Integer abInteger;
  
  public Integer getIntegerAB() {
    return abInteger;
  }
}