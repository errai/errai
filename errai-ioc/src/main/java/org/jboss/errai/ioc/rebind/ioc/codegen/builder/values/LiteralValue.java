package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public abstract class LiteralValue<T> implements Statement {
    private T value;

    public abstract String getCanonicalString();

    protected LiteralValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public String generate() {
        return getCanonicalString();
    }

    public Scope getScope() {
        return null;
    }
}
