package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class FooblieDependentBean {
  @Inject @Response
  Fooblie fooblieResponse;

  @Inject @Greets
  Fooblie fooblieGreets;

  @Inject @Parts
  Fooblie fooblieParts;

  public Fooblie getFooblieResponse() {
    return fooblieResponse;
  }

  public Fooblie getFooblieGreets() {
    return fooblieGreets;
  }

  public Fooblie getFooblieParts() {
    return fooblieParts;
  }
}
