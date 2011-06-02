package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableDeclaration;

import javax.enterprise.util.TypeLiteral;

/**
 * Builder for the {@link Context}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContextBuilder {
    private Context context;

    protected ContextBuilder(Context context) {
        this.context = context;
    }

    public static ContextBuilder create() {
        return new ContextBuilder(Context.create());
    }

    public static ContextBuilder create(Context context) {
        return new ContextBuilder(context);
    }

    public ContextBuilder addVariable(Variable variable) {
        context.addVariable(variable);
        return this;
    }

    public ContextBuilder addVariable(String name, Class<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }

    public ContextBuilder addVariable(String name, TypeLiteral<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }

    public ContextBuilder addVariable(String name, Object initialization) {
        context.addVariable(Variable.create(name, initialization));
        return this;
    }

    public ContextBuilder addVariable(String name, Class<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }

    public ContextBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }

    public VariableDeclaration declareVariable(final Variable var) {
        return new VariableDeclaration() {
            public Statement initializeWith(Object initialization) {
                var.initialize(initialization);
                return var;
            }

            public Statement initializeWith(Statement initialization) {
                var.initialize(initialization);
                return var;
            }
        };
    }

    public VariableDeclaration declareVariable(String name) {
        return declareVariable(Variable.create(name, (Class<?>) null));
    }

    public VariableDeclaration declareVariable(String name, Class<?> type) {
        return declareVariable(Variable.create(name, type));
    }

    public VariableDeclaration declareVariable(String name, TypeLiteral<?> type) {
        return declareVariable(Variable.create(name, type));
    }


    public Context getContext() {
        return context;
    }
}