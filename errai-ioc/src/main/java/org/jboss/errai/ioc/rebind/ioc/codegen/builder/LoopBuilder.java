package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.JavaReflectionClass;

/**
 * 
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
        this.sequenceVarName = scope.peekVariable().getName();
        this.loopVarName = loopVarName;
        return new LoopBodyBuilder(body = new BlockStatement());
    }
    
    public LoopBodyBuilder foreach(String loopVarName, String sequenceVarName) {
        this.sequenceVarName = sequenceVarName;
        this.loopVarName = loopVarName;
        return new LoopBodyBuilder(body = new BlockStatement());
    }
    
    private Variable createLoopVar(Variable sequenceVar) {
        // infer the loop variable type
        MetaClass loopVarType = new JavaReflectionClass(Object.class);
        if (sequenceVar.getType().getParameterizedTypes().length>0) {
            loopVarType = sequenceVar.getType().getParameterizedTypes()[0];
        } else if (getVariableComponentType(sequenceVar)!=null) {
            loopVarType = getVariableComponentType(sequenceVar);
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
