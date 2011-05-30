package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface VariableReferenceContextualStatement extends ContextualStatement {
    public Statement assignValue(Object statement);
}
