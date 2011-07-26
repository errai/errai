package org.jboss.errai.enterprise.rebind;

import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadata;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadataFactory;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JSR299QualfyingMetadataFactory implements QualifyingMetadataFactory {
  @Override
  public QualifyingMetadata createFrom(Annotation[] annotations) {
    return JSR299QualifyingMetadata.createFromAnnotations(annotations);
  }

  @Override
  public QualifyingMetadata createDefaultMetadata() {
    return JSR299QualifyingMetadata.createDefaultQualifyingMetaData();
  }
}
