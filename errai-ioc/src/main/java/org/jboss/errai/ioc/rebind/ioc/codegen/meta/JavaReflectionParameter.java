package org.jboss.errai.ioc.rebind.ioc.codegen.meta;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;
import org.mvel2.util.ReflectionUtil;

import java.lang.annotation.Annotation;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JavaReflectionParameter implements MetaParameter {
    private String name;
    private Class<?> type;
    private Annotation[] annotations;
    private MetaClassMember declaredBy;


    public JavaReflectionParameter(Class<?> type, Annotation[] annotations, MetaClassMember declaredBy) {
        this.name = ReflectionUtil.getPropertyFromAccessor(type.getSimpleName());
        this.type = type;
        this.annotations = annotations;
        this.declaredBy = declaredBy;
    }

    public String getName() {
        return name;
    }

    public MetaClass getType() {
        return new JavaReflectionClass(type);
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }


    public MetaClassMember getDeclaringMember() {
        return declaredBy;
    }
}
