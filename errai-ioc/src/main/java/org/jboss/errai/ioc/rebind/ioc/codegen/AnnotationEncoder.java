package org.jboss.errai.ioc.rebind.ioc.codegen;


import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;

import java.lang.annotation.Annotation;

public class AnnotationEncoder {
    public static String encode(Annotation annotation) {
        Class<? extends Annotation> annotationClass = annotation.annotationType();
        String str = ObjectBuilder.newInstanceOf(annotationClass)
                //         { extend the class type
                .extend()
                        // override the annotationType() method.
                .publicOverridesMethod("annotationType")
                        //   {
                .append(StatementBuilder.create().load(annotationClass).returnValue())
                        //   }
                .finish()
                        // }
                .finish()
                .toJavaString();

        System.out.println(str);

        return str;
    }
}
