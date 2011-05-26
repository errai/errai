package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
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
            super(LoopBuilder.this.getScope());
            this.blockStatement = blockStatement;
        }

        public LoopBodyBuilder execute(Statement statement) {
            blockStatement.addStatement(statement);
            statement.getScope().merge(scope);
            return this;
        }

        public String generate() {
            return LoopBuilder.this.generate();
        }
        
        public Scope getScope() {
            return scope;
        }
    }

    private String loopVarName;
    private MetaClass loopVarType;
    private Variable loopVar;
    private Statement collectionVar;
    private BlockStatement body;

    private LoopBuilder(Scope scope) {
        super(scope);
    }

    public static LoopBuilder create() {
        return new LoopBuilder(new Scope());
    }

    public static LoopBuilder createInScopeOf(HasScope parent) {
        return new LoopBuilder(parent.getScope());
    }

    public LoopBodyBuilder foreach(String loopVarName) {
        return foreach(loopVarName, null);
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return foreach(loopVarName, loopVarType, scope.peek());
    }

    private LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, Statement collectionVar) {
        assertIsIterable(collectionVar);

        this.collectionVar = collectionVar;
        this.loopVarName = loopVarName;
        this.loopVarType = loopVarType;
        this.loopVar = createLoopVar(collectionVar);
        return new LoopBodyBuilder(body = new BlockStatement());
    }
    
    private Variable createLoopVar(Statement collectionVar) {

        // infer the loop variable type
        MetaClass loopVarType = MetaClassFactory.get(Object.class);
        if (collectionVar.getType().getParameterizedTypes().length > 0) {
            loopVarType = collectionVar.getType().getParameterizedTypes()[0];
        } else if (getComponentType(collectionVar) != null) {
            loopVarType = getComponentType(collectionVar);
        }

        // try to use the provided loop var type if possible (assignable from the inferred type)
        if (this.loopVarType != null) {
            assertAssignableTypes(loopVarType, this.loopVarType);
            loopVarType = this.loopVarType;
        }

        Variable loopVar = new Variable(this.loopVarName, loopVarType);
        scope.push(loopVar);
        return loopVar;
    }

    public String generate() {
        buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
            .append(" : ").append(collectionVar.generate()).append(") {")
                .append("\n\t").append(body.generate().replaceAll("\n", "\n\t"))
            .append("\n}");

        return buf.toString();
    }
}
