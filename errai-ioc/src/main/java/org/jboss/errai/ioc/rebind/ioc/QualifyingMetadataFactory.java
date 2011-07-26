package org.jboss.errai.ioc.rebind.ioc;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface QualifyingMetadataFactory {
  public QualifyingMetadata createFrom(Annotation[] annotations);

  public QualifyingMetadata createDefaultMetadata();
}
