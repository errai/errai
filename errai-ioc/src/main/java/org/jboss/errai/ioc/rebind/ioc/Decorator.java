package org.jboss.errai.ioc.rebind.ioc;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;

import java.lang.annotation.Annotation;

public abstract class Decorator<T extends Annotation> {
    private final Class<T> decoratesWith;

    protected Decorator(Class<T> decoratesWith) {
        this.decoratesWith = decoratesWith;
    }

    public Class<T> decoratesWith() {
        return decoratesWith;
    }

    public abstract String generateDecorator(T annotation, TaskType taskType, JMethod method, JField field, JClassType type,
                                             Injector injector, InjectionContext ctx);
}
