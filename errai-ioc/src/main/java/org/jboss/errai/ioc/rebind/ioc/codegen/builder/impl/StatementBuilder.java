package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForeachLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.IfBlock;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

import javax.enterprise.util.TypeLiteral;

/**
 * The root of our fluent StatementBuilder API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder
        implements StatementBegin, ContextualStatementBuilder, VariableReferenceContextualStatementBuilder, StatementBegin {

    private StatementBuilder(StatementBuilder builder) {
        super(builder);
    }

    public static StatementBegin create() {
        return new StatementBuilder(null);
    }

    public StatementBuilder addVariable(String name, Class<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }

    public StatementBuilder addVariable(String name, Object initialization) {
        context.addVariable(Variable.create(name, initialization));
        return this;
    }

    public StatementBuilder addVariable(String name, Class<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }

    public VariableReferenceContextualStatementBuilder loadVariable(String name) {
        appendCallElement(new LoadVariable(name));
        return this;
    }

    // Start ContextualStatementBuilder stuff
    public ContextualStatementBuilder loadLiteral(Object o) {
        appendCallElement(new LoadLiteral(o));
        return this;
    }

    public ContextualStatementBuilder load(Object o) {
        appendCallElement(new DynamicLoad(o));
        return this;
    }

    // Method Invocation
    public ContextualStatementBuilder invoke(String methodName, Statement... parameters) {
        appendCallElement(new MethodCall(methodName, parameters));
        return this;
    }

    public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
        appendCallElement(new UnresolvedMethodCall(methodName, parameters));
        return this;
    }

    // Object Creation
    public ObjectBuilder newObject(MetaClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(JavaReflectionClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(Class<?> type) {
        return ObjectBuilder.newInstanceOf(type);
    }


    // Looping
    public BlockBuilder<LoopBuilder> foreach(String loopVarName) {
        return foreach(loopVarName, (MetaClass) null);
    }

    public BlockBuilder<LoopBuilder> foreach(String loopVarName, Class<?> loopVarType) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType));
    }

    public BlockBuilder<LoopBuilder> foreach(String loopVarName, MetaClass loopVarType) {
        BlockStatement body = new BlockStatement();

        appendCallElement(new DeferredCallElement(genBuilderCallback(loopVarName, loopVarType, body)));

        return new BlockBuilder<LoopBuilder>(body, new BuildCallback<LoopBuilder>() {
            public LoopBuilder callback(Statement statement) {
                return StatementBuilder.this;
            }
        });
    }

    private DeferredCallback genBuilderCallback(final String loopVarName,
                                                final MetaClass loopVarType,
                                                final BlockStatement body) {
        return new DeferredCallback() {
            public String doDeferred(Context context, Statement statement) {
                GenUtil.assertIsIterable(statement);

                Variable loopVar = createLoopVar(statement, loopVarName, loopVarType);

                return new ForeachLoop(loopVar, statement, body).generate(context);
            }
        };
    }

    private Variable createLoopVar(Statement collection, String loopVarName, MetaClass providedLoopVarType) {

        // infer the loop variable type
        MetaClass loopVarType = MetaClassFactory.get(Object.class);

        MetaParameterizedType parameterizedType = collection.getType().getParameterizedType();

        if (parameterizedType != null && parameterizedType.getTypeParameters().length != 0) {
            loopVarType = (MetaClass) parameterizedType.getTypeParameters()[0];
        } else if (GenUtil.getComponentType(collection) != null) {
            loopVarType = GenUtil.getComponentType(collection);
        }

        // try to use the provided loop variable type if possible (assignable from the inferred type)
        if (providedLoopVarType != null) {
            GenUtil.assertAssignableTypes(loopVarType, providedLoopVarType);
            loopVarType = providedLoopVarType;
        }

        Variable loopVar = Variable.create(loopVarName, loopVarType);
        context.addVariable(loopVar);
        return loopVar;
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
//        return new AssignmentBuilder(operator,
//                (VariableReference) this.statement, GenUtil.generate(context, statement));
        return null;
    }
}