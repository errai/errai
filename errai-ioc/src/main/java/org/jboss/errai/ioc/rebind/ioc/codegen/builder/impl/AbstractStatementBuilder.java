package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Builder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
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
    protected CallElementBuilder callElementBuilder;

    protected AbstractStatementBuilder(Context context, CallElementBuilder callElementBuilder) {
        this(context);
        this.callElementBuilder = callElementBuilder;
    }

    protected AbstractStatementBuilder(Context context) {
        if (context == null) {
            context = Context.create();
        }

        this.context = context;
        this.callElementBuilder = new CallElementBuilder();
    }

    public Context getContext() {
        return context;
    }

    public String generate(Context context) {
        CallWriter writer = new CallWriter();
        callElementBuilder.getRootElement().handleCall(writer, context, null);
        return writer.getCallString();
    }

    public void appendCallElement(CallElement element) {
        callElementBuilder.appendCallElement(element);
    }

    public MetaClass getType() {
        return callElementBuilder.getCallElement().getResultType();
    }

    public String toJavaString() {
        return generate(context);
    }
}
