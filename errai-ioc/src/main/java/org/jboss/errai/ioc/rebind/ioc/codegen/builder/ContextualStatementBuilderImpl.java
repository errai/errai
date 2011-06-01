package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextualStatementBuilderImpl extends AbstractStatementBuilder implements ContextualStatementBuilder,
        VariableReferenceContextualStatementBuilder {

    protected ContextualStatementBuilderImpl(AbstractStatementBuilder parent) {
        super(parent.context);
        this.statement = parent.statement;
    }

    public static ContextualStatementBuilderImpl createInContextOf(AbstractStatementBuilder parent) {
        return new ContextualStatementBuilderImpl(parent);
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return LoopBuilderImpl.create(this).foreach(loopVarName);
    }

    public LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType) {
        return LoopBuilderImpl.create(this).foreach(loopVarName, loopVarType);
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return LoopBuilderImpl.create(this).foreach(loopVarName, loopVarType);
    }

    public ElseBlockBuilder if_(Statement block) {
        return IfBlockBuilderImpl.create(this).if_(block);
    }
    
    public IfBlock if_(Statement block, IfBlock elseIf) {
        return IfBlockBuilderImpl.create(this).if_(block, elseIf);
    }
    
    public ElseBlockBuilder if_(BooleanOperator op, Statement rhs, Statement block) {
        return IfBlockBuilderImpl.create(this).if_(op, rhs, block);
    }
    
    public IfBlock if_(BooleanOperator op, Statement rhs, Statement block, IfBlock elseIf) {
        return IfBlockBuilderImpl.create(this).if_(op, rhs, block, elseIf);
    }

    public ElseBlockBuilder if_(BooleanOperator op, Object rhs, Statement block) {
        return IfBlockBuilderImpl.create(this).if_(op, rhs, block);
    }
    
    public IfBlock if_(BooleanOperator op, Object rhs, Statement block, IfBlock elseIf) {
        return IfBlockBuilderImpl.create(this).if_(op, rhs, block, elseIf);
    }
    
    public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
        return InvocationBuilder.create(this).invoke(methodName, parameters);
    }

    public Statement assignValue(Object statement) {
        return assignValue(AssignmentOperator.Assignment, statement);
    }

    public Statement assignValue(AssignmentOperator operator, Object statement) {
        return new AssignmentBuilder(operator,
                (VariableReference) this.statement, GenUtil.generate(context, statement));
    }
}
