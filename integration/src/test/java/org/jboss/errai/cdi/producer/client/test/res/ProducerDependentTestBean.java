package org.jboss.errai.cdi.producer.client.test.res;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.cdi.client.qualifier.D;

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

  @Inject
  Float unqualifiedFloat;
  
  @Inject @A @D
  Float adFloat;
  
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
  
  public Float getUnqualifiedFloat() {
    return unqualifiedFloat;
  }
  
  public Float getFloatAD() {
    return adFloat;
  }
}