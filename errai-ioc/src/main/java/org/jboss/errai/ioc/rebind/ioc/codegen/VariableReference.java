package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface VariableReference extends Statement {
    public String getName();
    public Statement getValue();
}
