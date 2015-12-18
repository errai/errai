package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@EntryPoint
public class QualInspector {
  @Inject
  @AQual
  private GenericService<?> aQualService;

  @Inject
  @BQual
  private GenericService<?> bQualService;

  public static QualInspector INSTANCE;

  @PostConstruct
  private void init() {
    INSTANCE = this;
  }

  public GenericService<?> getaQualService() {
    return aQualService;
  }

  public GenericService<?> getbQualService() {
    return bQualService;
  }
}
