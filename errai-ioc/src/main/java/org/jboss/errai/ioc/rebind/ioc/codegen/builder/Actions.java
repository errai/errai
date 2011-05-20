package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Actions {
    public StatementReference newObject(MetaClass reference);
    public StatementReference loadVariable(VariableReference variableReference);
    public LoopBuilder loop(VariableReference reference);
}
