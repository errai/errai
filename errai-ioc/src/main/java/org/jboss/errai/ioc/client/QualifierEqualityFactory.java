package org.jboss.errai.ioc.client;

import java.lang.annotation.Annotation;

/**
 * An interface which is implemented by the code generator to allow equality tests on annotations which consider
 * their attributes as part of the equality check.
 *
 * @author Mike Brock
 */
public interface QualifierEqualityFactory {
  public boolean isEqual(Annotation a1, Annotation a2);

  public int hashCodeOf(Annotation a1);
}
