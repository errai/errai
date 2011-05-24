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
    public class LoopBody implements Statement, HasScope {
        private BlockStatement blockStatement = null;
        
        private LoopBody(BlockStatement blockStatement) {
            this.blockStatement = blockStatement;
        }
        
        public LoopBody addStatement(Statement statement) {
            blockStatement.addStatement(statement);
            return this;
        }
        
        public String getStatement() {
            return LoopBuilder.this.getStatement();
        }
        
        public Scope getScope() {
            return LoopBuilder.this.getScope();
        }
    }
    
    private Variable loopVar;
    private Variable sequenceVar;
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
    
    public LoopBody loop(Variable loopVar, Variable sequenceVar) {
        assertVariableInScope(loopVar);
        assertVariableInScope(sequenceVar);
        
        this.loopVar = loopVar;
        this.sequenceVar = sequenceVar;
        this.body = new BlockStatement();
        return new LoopBody(body);
    }
    
    public String getStatement() {
        StringBuilder buf = new StringBuilder();

        buf.append("for (").append(loopVar.getType().getFullyQualifedName()).append(" ").append(loopVar.getName())
            .append(" : ").append(sequenceVar.getName()).append(") {")
                .append("\n\t").append(body.getStatement().replaceAll("\n", "\n\t"))
            .append("\n};");
        
        return buf.toString();
    }
}
