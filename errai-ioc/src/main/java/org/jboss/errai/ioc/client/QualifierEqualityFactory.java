package org.jboss.errai.ioc.client;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock
 */
public interface QualifierEqualityFactory {
  public boolean isEqual(Annotation a1, Annotation a2);
}
