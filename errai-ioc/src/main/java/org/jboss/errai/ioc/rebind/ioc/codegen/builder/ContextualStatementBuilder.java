package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder.LoopBodyBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextualStatementBuilder extends AbstractStatementBuilder implements ContextualStatement, VariableReferenceContextualStatement {

    protected ContextualStatementBuilder(AbstractStatementBuilder parent) {
        super(parent.context);
        this.statement = parent.statement;
    }

    public static ContextualStatementBuilder createInContextOf(AbstractStatementBuilder parent) {
        return new ContextualStatementBuilder(parent);
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return LoopBuilder.create(this).foreach(loopVarName);
    }

    public LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType));
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return LoopBuilder.create(this).foreach(loopVarName, loopVarType);
    }

    public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
        return InvocationBuilder.create(this).invoke(methodName, parameters);
    }

    public Statement assignValue(Object statement) {
        return new AssignmentBuilder((VariableReference) this.statement, GenUtil.generate(context, statement));
    }
}
