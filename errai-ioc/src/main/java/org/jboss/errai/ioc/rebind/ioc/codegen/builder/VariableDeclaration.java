package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface VariableDeclaration {
    public Statement initializeWith(Object initialization);

    public Statement initializeWith(Statement initialization);
}
