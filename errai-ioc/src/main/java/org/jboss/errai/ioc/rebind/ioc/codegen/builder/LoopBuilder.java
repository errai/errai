package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.control.ForeachLoop;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * StatementBuilder to generate loops.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilder extends AbstractStatementBuilder {

    public class LoopBodyBuilder extends AbstractStatementBuilder {
        private BlockStatement blockStatement = null;

        private LoopBodyBuilder(BlockStatement blockStatement) {
            super(LoopBuilder.this.getContext());
            this.blockStatement = blockStatement;
        }

        public LoopBodyBuilder execute(Statement statement) {
            blockStatement.addStatement(statement);
            return this;
        }

        public String generate() {
            return LoopBuilder.this.generate();
        }
        
        public Context getContext() {
            return context;
        }
    }

    private Statement loop;
    
    private LoopBuilder(Context context) {
        super(context);
    }

    public static LoopBuilder createInContextOf(Statement parent) {
        return new LoopBuilder(Context.create(parent.getContext()));
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return foreach(loopVarName, null);
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return foreach(loopVarName, loopVarType, context.getStatement());
    }

    private LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, Statement collection) {
        assertIsIterable(collection);

        Variable loopVar = createLoopVar(collection, loopVarName, loopVarType);
        BlockStatement body = new BlockStatement();
        loop = new ForeachLoop(loopVar, collection, body);
        
        return new LoopBodyBuilder(body);
    }
    
    private Variable createLoopVar(Statement collection, String loopVarName, MetaClass providedLoopVarType) {

        // infer the loop variable type
        MetaClass loopVarType = MetaClassFactory.get(Object.class);
        if (collection.getType().getParameterizedTypes().length > 0) {
            loopVarType = collection.getType().getParameterizedTypes()[0];
        } else if (getComponentType(collection) != null) {
            loopVarType = getComponentType(collection);
        }

        // try to use the provided loop variable type if possible (assignable from the inferred type)
        if (providedLoopVarType != null) {
            assertAssignableTypes(loopVarType, providedLoopVarType);
            loopVarType = providedLoopVarType;
        }

        Variable loopVar = Variable.get(loopVarName, loopVarType);
        context.add(loopVar);
        return loopVar;
    }

    public String generate() {
        return loop.generate();
    }
}
