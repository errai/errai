package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
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
            ((Variable) statement).initialize(initialization);
            return statement;
        }
    }

    private VariableDeclarationBuilder(Context context) {
        super(context);
    }

    public static VariableDeclarationBuilder createInContextOf(Context context) {
        return new VariableDeclarationBuilder(context);
    }

    public VariableInitializationBuilder declareVariable(Variable var) {
        statement = var;
        return new VariableInitializationBuilder();
    }

    public VariableInitializationBuilder declareVariable(String name) {
        statement = Variable.create(name, (Class<?>) null);
        return new VariableInitializationBuilder();
    }
}