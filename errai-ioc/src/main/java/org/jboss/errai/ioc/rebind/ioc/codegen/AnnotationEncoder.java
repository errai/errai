package org.jboss.errai.ioc.rebind.ioc.codegen;


import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ObjectBuilder;

import java.lang.annotation.Annotation;

public class AnnotationEncoder {
    public static String encode(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();

        ObjectBuilder builder = ObjectBuilder.newInstanceOf(annotationClass);
        ClassStructureBuilder classStructureBuilder = builder.extend();

        Statement statement = new StringStatement("return " + annotationClass.getName() + ".class;");

        return classStructureBuilder
                .publicOverridesMethod("annotationType")
                .append(statement)
                .finish()
                .finish()
                .toJavaString();
    }
}
