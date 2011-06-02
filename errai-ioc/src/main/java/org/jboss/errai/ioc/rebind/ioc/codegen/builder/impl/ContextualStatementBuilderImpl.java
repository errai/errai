package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.AssignmentOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.BooleanOperator;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.StringStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ElseBlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.MethodCall;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;

/**
 * Implementation of the {@link ContextualStatementBuilder}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextualStatementBuilderImpl extends AbstractStatementBuilder implements ContextualStatementBuilder,
        VariableReferenceContextualStatementBuilder {

    protected ContextualStatementBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
        super(context, callElementBuilder);
    }

    // Invocation
    public ContextualStatementBuilder invoke(String methodName, Statement... parameters) {
        appendCallElement(new MethodCall(methodName, parameters));
        return this;
    }

    public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
        appendCallElement(new MethodCall(methodName, parameters));
        return this;
    }
    
    // Looping
    public BlockBuilder<LoopBuilder> foreach(String loopVarName) {
        return new LoopBuilderImpl(context, callElementBuilder).foreach(loopVarName);
    }

    public BlockBuilder<LoopBuilder> foreach(String loopVarName, Class<?> loopVarType) {
        return new LoopBuilderImpl(context, callElementBuilder).foreach(loopVarName, loopVarType);
    }

    // If-Then-Else
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

    // Value return
    public Statement returnValue() {
        return new StringStatement("return " + toJavaString() + ";");
    }

    // Assignments
    public Statement assignValue(Object statement) {
        return assignValue(AssignmentOperator.Assignment, statement);
    }

    public Statement assignValue(AssignmentOperator operator, Object statement) {
        //return new AssignmentBuilder(operator,
                //(VariableReference) this.statement, GenUtil.generate(context, statement));
        return null;
    }
}
