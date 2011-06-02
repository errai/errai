package org.jboss.errai.ioc.rebind.ioc.codegen;


import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBuilder;

import java.lang.annotation.Annotation;

public class AnnotationEncoder {
    public static String encode(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        return ObjectBuilder.newInstanceOf(annotationClass)
                // { extend the class type
                .extend()
                        // override the annotationType() method.
                .publicOverridesMethod("annotationType")
                        // {
                .append(StatementBuilder.create().load(annotationClass).returnValue())
                        // }
                .finish()
                        // }
                .finish()
                .toJavaString();
    }
}
