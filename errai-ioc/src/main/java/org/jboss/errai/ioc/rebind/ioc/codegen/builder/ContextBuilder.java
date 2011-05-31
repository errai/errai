package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableDeclarationBuilder.VariableInitializationBuilder;

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
    
    public VariableInitializationBuilder declareVariable(String name, Class<?> type) {
        return VariableDeclarationBuilder.createInContextOf(this).declareVariable(Variable.create(name, type));
    }

    public VariableInitializationBuilder declareVariable(String name, TypeLiteral<?> type) {
        return VariableDeclarationBuilder.createInContextOf(this).declareVariable(Variable.create(name, type));
    }

    public VariableInitializationBuilder declareVariable(String name) {
        return VariableDeclarationBuilder.createInContextOf(this).declareVariable(name);
    }
    
    public Context getContext() {
        return context;
    }
}