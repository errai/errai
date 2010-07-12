package org.jboss.errai.ioc.rebind.ioc;

import java.lang.annotation.Annotation;

public abstract class Decorator<T extends Annotation> {
    private final Class<T> decoratesWith;

    protected Decorator(Class<T> decoratesWith) {
        this.decoratesWith = decoratesWith;
    }

    public Class<T> decoratesWith() {
        return decoratesWith;
    }

    public abstract String generateDecorator(DecoratorContext<T> ctx);
}
