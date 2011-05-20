package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface StatementReference {
    public StatementReference invokeMethod(String name, Class... parms);
    public LoopBuilder loop(VariableReference reference);
}
