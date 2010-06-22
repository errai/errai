package org.jboss.errai.ioc.rebind;

import com.google.gwt.core.ext.typeinfo.JClassType;
import org.jboss.errai.bus.rebind.ProcessingContext;

import java.lang.annotation.Annotation;

public interface AnnotationHandler<T extends Annotation> {
    public void handle(JClassType type, T annotation, ProcessingContext context);
}
