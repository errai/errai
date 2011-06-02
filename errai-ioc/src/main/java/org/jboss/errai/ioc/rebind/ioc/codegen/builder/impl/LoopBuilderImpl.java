package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.BuildCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.LoopBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.CallWriter;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallElement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.DeferredCallback;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForeachLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;

/**
 * StatementBuilder to generate loops.
 *  
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilderImpl extends AbstractStatementBuilder implements LoopBuilder {

    protected LoopBuilderImpl(Context context, CallElementBuilder callElementBuilder) {
        super(context, callElementBuilder);
    }

    public BlockBuilder<LoopBuilder> foreach(String loopVarName) {
        return foreach(loopVarName, (MetaClass) null);
    }

    public BlockBuilder<LoopBuilder> foreach(String loopVarName, Class<?> loopVarType) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType));
    }

    private BlockBuilder<LoopBuilder> foreach(String loopVarName, MetaClass loopVarType) {
        BlockStatement body = new BlockStatement();
        appendCallElement(new DeferredCallElement(genBuilderCallback(loopVarName, loopVarType, body)));
        
        return new BlockBuilder<LoopBuilder>(body, new BuildCallback<LoopBuilder>() {
            public LoopBuilder callback(Statement statement) {
                return LoopBuilderImpl.this;
            }
        });
    }

    private DeferredCallback genBuilderCallback(final String loopVarName, final MetaClass loopVarType,
            final BlockStatement body) {
        
        return new DeferredCallback() {
            public void doDeferred(CallWriter writer, Context context, Statement statement) {
                GenUtil.assertIsIterable(statement);

                Variable loopVar = createLoopVar(statement, loopVarName, loopVarType);
                String collectionExpr = writer.getCallString();
                // destroy the buffer up until now.
                writer.reset();
                writer.append(new ForeachLoop(loopVar, collectionExpr, body).generate(Context.create(context)));
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
}
