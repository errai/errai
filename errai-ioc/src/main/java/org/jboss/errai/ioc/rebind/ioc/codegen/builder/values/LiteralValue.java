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

    @Override
    public int hashCode() {
        return (value == null) ? 0 : value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LiteralValue<?> other = (LiteralValue<?>) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}