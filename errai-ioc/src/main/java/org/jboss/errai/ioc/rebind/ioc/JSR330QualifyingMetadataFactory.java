package org.jboss.errai.ioc.rebind.ioc;

import org.jboss.errai.ioc.rebind.ioc.JSR330QualifyingMetadata;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadata;
import org.jboss.errai.ioc.rebind.ioc.QualifyingMetadataFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JSR330QualifyingMetadataFactory implements QualifyingMetadataFactory {
  @Override
  public QualifyingMetadata createFrom(Annotation[] annotations) {
    return JSR330QualifyingMetadata.createFromAnnotations(annotations);
  }

  @Override
  public QualifyingMetadata createDefaultMetadata() {
    return JSR330QualifyingMetadata.createDefaultQualifyingMetaData();
  }
}
