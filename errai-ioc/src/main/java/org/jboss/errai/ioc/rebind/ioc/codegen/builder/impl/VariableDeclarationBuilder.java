package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

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
            //statement = VariableDeclarationBuilder.this.statement;
        }

        public Statement initializeWith(Object initialization) {
//            ((Variable) statement).initialize(initialization);
//            return statement;
            return null;
        }

        public Statement initializeWith(Statement initialization) {
//            ((Variable) statement).initialize(initialization);
//            return statement;
            return null;
        }
    }

    public VariableDeclarationBuilder(Context context) {
        super(context);
    }

    //    private VariableDeclarationBuilder(ContextBuilder parent) {
//        super(parent);
//    }

    public static VariableDeclarationBuilder createInContextOf(Context context) {
        return new VariableDeclarationBuilder(context);
    }

    public VariableInitializationBuilder declareVariable(Variable var) {
        context.addVariable(var);
        //     statement = var;
        return new VariableInitializationBuilder();
    }

    public VariableInitializationBuilder declareVariable(String name) {
        return declareVariable(Variable.create(name, (Class<?>) null));
    }
}