package org.jboss.errai.ioc.client;


import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface ContextualProviderContext {
  public Annotation[] getQualifiers();

  public Class<?>[] getTypeArguments();
}
