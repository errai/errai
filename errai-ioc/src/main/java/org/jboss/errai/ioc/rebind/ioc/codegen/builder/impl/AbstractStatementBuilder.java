package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.AbstractCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * Base class of all {@link StatementBuilder}s
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements Statement, Builder {
    protected Context context = null;
    protected CallElement callElement;
    protected AbstractStatementBuilder parent = null;

    protected AbstractStatementBuilder(Context context) {
        this.context = context;
    }

    protected AbstractStatementBuilder(AbstractStatementBuilder parent) {
        this.parent = parent;
        if (parent == null || parent.context == null) {
            this.context = Context.create();
        } else {
            this.context = parent.context;
        }
    }

    public Context getContext() {
        return context;
    }

    public String generate(Context context) {
        return getAbsoluteParent().callElement.getStatement(context, null);
    }

    public void appendCallElement(CallElement element) {
        AbstractStatementBuilder builder = getAbsoluteParent();
        if (builder.callElement == null) {
            builder.callElement = element;
        } else {
            AbstractCallElement.append(builder.callElement, element);
        }
    }

    private AbstractStatementBuilder getAbsoluteParent() {
        AbstractStatementBuilder builder = this;
        while (builder.parent != null) builder = builder.parent;

        return builder;
    }

    public MetaClass getType() {
//        if (statement != null) {
//            return statement.getType();
//        }
        return MetaClassFactory.get(Void.class);
    }

    public String toJavaString() {
        //TODO generate(context)
        AbstractStatementBuilder builder = this;
        while (builder.parent != null) builder = builder.parent;

        return builder.generate(context);
    }
}
