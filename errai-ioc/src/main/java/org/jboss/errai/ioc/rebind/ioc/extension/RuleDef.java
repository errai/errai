package org.jboss.errai.ioc.rebind.ioc.extension;

import java.lang.annotation.Annotation;

/**
* @author Mike Brock
*/
public class RuleDef {
  private Class<? extends Annotation> relAnnotation;
  private RelativeOrder order;

  public RuleDef(Class<? extends Annotation> relAnnotation, RelativeOrder order) {
    this.relAnnotation = relAnnotation;
    this.order = order;
  }

  public Class<? extends Annotation> getRelAnnotation() {
    return relAnnotation;
  }

  public RelativeOrder getOrder() {
    return order;
  }
}
