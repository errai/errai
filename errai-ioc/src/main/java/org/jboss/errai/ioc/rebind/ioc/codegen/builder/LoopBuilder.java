package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class LoopBuilder extends AbstractStatementBuilder implements Statement {

    public class LoopBodyBuilder extends AbstractStatementBuilder implements Statement {
        private BlockStatement blockStatement = null;

        private LoopBodyBuilder(BlockStatement blockStatement) {
            super(LoopBuilder.this.getScope());
            this.blockStatement = blockStatement;
        }

        public LoopBodyBuilder addStatement(Statement statement) {
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
    private String sequenceVarName;
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
        return foreach(loopVarName, scope.peekVariable().getName());
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType) {
        return foreach(loopVarName, loopVarType, scope.peekVariable().getName());
    }

    public LoopBodyBuilder foreach(String loopVarName, String sequenceVarName) {
        return foreach(loopVarName, null, sequenceVarName);
    }

    public LoopBodyBuilder foreach(String loopVarName, MetaClass loopVarType, String sequenceVarName) {
        this.loopVarName = loopVarName;
        this.loopVarType = loopVarType;
        this.sequenceVarName = sequenceVarName;
        return new LoopBodyBuilder(body = new BlockStatement());
    }

    private Variable createLoopVar(Variable sequenceVar) {

        // infer the loop variable type
        MetaClass loopVarType = new JavaReflectionClass(Object.class);
        if (sequenceVar.getType().getParameterizedTypes().length > 0) {
            loopVarType = sequenceVar.getType().getParameterizedTypes()[0];
        } else if (getVariableComponentType(sequenceVar) != null) {
            loopVarType = getVariableComponentType(sequenceVar);
        }

        // try the use the provided loop var type if possible
        if (this.loopVarType != null) {
            assertAssignableTypes(loopVarType, this.loopVarType);
            loopVarType = this.loopVarType;
        }

        Variable loopVar = new Variable(loopVarName, loopVarType);
        scope.pushVariable(loopVar);
        return loopVar;
    }

    public String generate() {
        assertVariableInScope(sequenceVarName);
        Variable sequenceVar = scope.getVariable(sequenceVarName);
        assertVariableIsIterable(sequenceVar);

        Variable loopVar = createLoopVar(sequenceVar);

        StringBuilder buf = new StringBuilder();
        buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
                .append(" : ").append(sequenceVar.getName()).append(") {")
                .append("\n\t").append(body.generate().replaceAll("\n", "\n\t"))
                .append("\n};");

        return buf.toString();
    }
}
