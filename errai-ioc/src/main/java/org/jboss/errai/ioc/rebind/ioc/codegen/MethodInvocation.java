package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * Represents a method invocation statement.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MethodInvocation extends AbstractStatement {
    private final Statement target;
    private final MetaMethod method;
    private final CallParameters parameters;

    public MethodInvocation(Statement target, MetaMethod method, CallParameters parameters) {
        this.target = target;
        this.method = method;
        this.parameters = parameters;
    }

    public String generate() {
        StringBuilder buf = new StringBuilder();
        buf.append(target.generate()).append(".").append(method.getName()).append(parameters.generate());
        return buf.toString();
    }
    
    public MetaClass getType() {
        return method.getReturnType();
    }
}