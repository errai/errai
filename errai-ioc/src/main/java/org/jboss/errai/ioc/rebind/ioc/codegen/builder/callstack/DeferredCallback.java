package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * An element for deferring and offloading validation and generation work for building the
 * call stack.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface DeferredCallback {
    public void doDeferred(CallWriter writer, Context context, Statement statement);
}
