package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;

/**
 * StatementBuilder to generate variable declarations.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class VariableDeclarationBuilder extends AbstractStatementBuilder {
    
    public class VariableInitializationBuilder extends AbstractStatementBuilder {
        private VariableInitializationBuilder() {
            super(VariableDeclarationBuilder.this.context);
            statement = VariableDeclarationBuilder.this.statement;
        }
        
        public Statement initializeWith(Object initialization) {
            ((Variable)statement).initialize(GenUtil.generate(context, initialization));
            return statement;
        }
    }
    
    private VariableDeclarationBuilder(AbstractStatementBuilder parent) {
        super(Context.create(parent.getContext()));
        this.parent = parent;
    }

    public static VariableDeclarationBuilder create(AbstractStatementBuilder parent) {
        return new VariableDeclarationBuilder(parent);
    }
    
    public VariableInitializationBuilder declareVariable(Variable var) {
        statement = var;
        return new VariableInitializationBuilder();
    }
    
    public VariableInitializationBuilder declareVariable(String name) {
        statement = Variable.get(name, (Class<?>) null);
        return new VariableInitializationBuilder();
    }
}