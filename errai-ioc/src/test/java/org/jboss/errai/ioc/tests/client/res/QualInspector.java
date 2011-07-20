package org.jboss.errai.ioc.tests.client.res;

import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author Mike Brock <cbrock@redhat.com>
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
  public void init() {
    INSTANCE = this;
  }

  public GenericService<?> getaQualService() {
    return aQualService;
  }

  public GenericService<?> getbQualService() {
    return bQualService;
  }
}
