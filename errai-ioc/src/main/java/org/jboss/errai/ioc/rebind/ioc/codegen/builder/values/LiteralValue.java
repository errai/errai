package org.jboss.errai.ioc.rebind.ioc.codegen.builder.values;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

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

    public Context getContext() {
        return null;
    }
    
    public MetaClass getType() {
        return MetaClassFactory.get(value.getClass());
    }
}
