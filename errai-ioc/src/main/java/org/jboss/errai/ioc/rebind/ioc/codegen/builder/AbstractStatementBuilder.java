package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.AbstractStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidTypeException;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.TypeNotIterableException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder extends AbstractStatement {
    protected StringBuilder buf = new StringBuilder();
    protected Context context = null;

    protected AbstractStatementBuilder(Context context) {
        this.context = context;
    }

    protected void assertIsIterable(Statement statement) {
        try {
            Class<?> cls = Class.forName(statement.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (!cls.isArray() && !Iterable.class.isAssignableFrom(cls))
                throw new TypeNotIterableException(statement.generate());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected MetaClass getComponentType(Statement statement) {
        try {
            Class<?> cls = Class.forName(statement.getType().getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (cls.getComponentType() != null)
                return MetaClassFactory.get(cls.getComponentType());

            return null;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void assertAssignableTypes(MetaClass from, MetaClass to) {
        try {
            Class<?> fromCls = Class.forName(from.getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            Class<?> toCls = Class.forName(to.getFullyQualifedName(), false,
                    Thread.currentThread().getContextClassLoader());

            if (!toCls.isAssignableFrom(fromCls))
                throw new InvalidTypeException(to.getFullyQualifedName() +
                        " is not assignable from " + from.getFullyQualifedName());

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Context getContext() {
        return context;
    }
    
    public String generate() {
        if(buf.length()==0) 
            return context.getStatement().generate();
        
        return buf.toString();
    }
}
