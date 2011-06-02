package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DeferredCallElement<T> extends AbstractCallElement {
    private DeferredCallback callback;

    public DeferredCallElement(DeferredCallback callback) {
        this.callback = callback;
    }

    public String getStatement(Context context, Statement statement) {
        if (next != null) {
            return callback.doDeferred(context, statement) + "." + next.getStatement(context, statement);
        } else {
            return callback.doDeferred(context, statement);
        }
    }
}
