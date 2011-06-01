package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForeachLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaParameterizedType;

/**
 * StatementBuilder to generate loops.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilderImpl extends AbstractStatementBuilder implements LoopBuilder {

    public class LoopBodyBuilderImpl extends AbstractStatementBuilder implements LoopBodyBuilder {
        private BlockStatement blockStatement = null;

        private LoopBodyBuilderImpl(BlockStatement blockStatement) {
            super(LoopBuilderImpl.this.getContext());
            this.blockStatement = blockStatement;
        }

        public LoopBodyBuilder execute(Statement statement) {
            blockStatement.addStatement(statement);
            return this;
        }

        public String generate() {
            return LoopBuilderImpl.this.generate();
        }

        public Context getContext() {
            return context;
        }
    }

    private LoopBuilderImpl(AbstractStatementBuilder parent) {
        super(Context.create(parent.getContext()));
        this.parent = parent;
    }

    public static LoopBuilderImpl create(AbstractStatementBuilder parent) {
        return new LoopBuilderImpl(parent);
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return foreach(loopVarName, (MetaClass) null);
    }

    public LoopBodyBuilder foreach(String loopVarName, Class<?> loopVarType) {
        return foreach(loopVarName, MetaClassFactory.get(loopVarType));
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return foreach(loopVarName, loopVarType, parent.statement);
    }

    private LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, Statement collection) {
        GenUtil.assertIsIterable(collection);

        Variable loopVar = createLoopVar(collection, loopVarName, loopVarType);
        BlockStatement body = new BlockStatement();
        statement = new ForeachLoop(loopVar, collection, body);

        return new LoopBodyBuilderImpl(body);
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
