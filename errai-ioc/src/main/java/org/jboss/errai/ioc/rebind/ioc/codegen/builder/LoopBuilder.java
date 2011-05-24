package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.BlockStatement;
import org.jboss.errai.ioc.rebind.ioc.codegen.HasScope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Scope;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;

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
    
    public LoopBodyBuilder loop(String loopVarName, String sequenceVarName) {
        this.loopVarName = loopVarName;
        this.sequenceVarName = sequenceVarName;
        this.body = new BlockStatement();
        return new LoopBodyBuilder(body);
    }
    
    public String generate() {
        assertVariableInScope(loopVarName);
        assertVariableInScope(sequenceVarName);

        Variable loopVar = scope.getVariable(loopVarName);
        Variable sequenceVar = scope.getVariable(sequenceVarName);
        
        StringBuilder buf = new StringBuilder();
        buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
            .append(" : ").append(sequenceVar.getName()).append(") {")
                .append("\n\t").append(body.generate().replaceAll("\n", "\n\t"))
            .append("\n};");

        return buf.toString();
    }
}
