package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder.LoopBody;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.GWTClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;

/**
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder {

    private StatementBuilder(Scope scope) {
        super(scope);
    }

    public static StatementBuilder create() {
        return new StatementBuilder(new Scope());
    }

    public static StatementBuilder createInScopeOf(HasScope parent) {
        return new StatementBuilder(parent.getScope());
    }

    public StatementBuilder loadVariable(String name, MetaClass type) {
        scope.addVariable(name, new Variable(name, type));
        return this;
    }

    public ObjectBuilder newObject(GWTClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(JavaReflectionClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public LoopBody loop(String loopVarName, String sequenceVarName) {
        return LoopBuilder.createInScopeOf(this).loop(scope.getVariable(loopVarName), scope.getVariable(sequenceVarName));
    }
}
