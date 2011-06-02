package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.AbstractCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * Base class of all {@link StatementBuilder}s
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public abstract class AbstractStatementBuilder implements Statement, Builder {
    protected Context context = null;
    protected CallElement callElement;

    protected AbstractStatementBuilder(Context context) {
        if (context == null) {
            context = Context.create();
        }

        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public String generate(Context context) {
        CallWriter writer = new CallWriter();
        callElement.handleCall(writer, context, null);
        return writer.getCallString();
    }

    public void appendCallElement(CallElement element) {
        if (callElement == null) {
            callElement = element;
        } else {
            AbstractCallElement.append(callElement, element);
        }
    }

    public MetaClass getType() {
//        if (statement != null) {
//            return statement.getType();
//        }
        return MetaClassFactory.get(Void.class);
    }

    public String toJavaString() {
        return generate(context);
    }
}
