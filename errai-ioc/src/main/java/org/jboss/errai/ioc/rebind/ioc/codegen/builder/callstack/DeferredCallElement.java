package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DeferredCallElement extends AbstractCallElement {
    private DeferredCallback callback;

    public DeferredCallElement(DeferredCallback callback) {
        this.callback = callback;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        callback.doDeferred(writer, context, statement);

        if (next != null) {
            writer.append(".");
            getNext().handleCall(writer, context, statement);
        }
    }
}