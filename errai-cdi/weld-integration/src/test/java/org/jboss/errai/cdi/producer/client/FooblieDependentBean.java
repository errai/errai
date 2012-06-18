package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class FooblieDependentBean {
  @Inject
  Fooblie fooblie;

  public Fooblie getFooblie() {
    return fooblie;
  }
}
