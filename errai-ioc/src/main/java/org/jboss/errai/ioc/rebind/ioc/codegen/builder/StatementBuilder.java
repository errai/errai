package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

/**
 * The root of our fluent StatementBuilder API.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder {

    private StatementBuilder(Context context) {
        super(context);
    }

    public static StatementBuilder create() {
        return new StatementBuilder(Context.create());
    }

    public static StatementBuilder create(Context context) {
        return new StatementBuilder(context);
    }
    
    public static StatementBuilder create(ContextBuilder context) {
        return new StatementBuilder(context.getContext());
    }
    
    public StatementBuilder addVariable(String name, Class<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type) {
        context.addVariable(Variable.create(name, type));
        return this;
    }
    
    public StatementBuilder addVariable(String name, Object initialization) {
        context.addVariable(Variable.create(name, initialization));
        return this;
    }
    
    public StatementBuilder addVariable(String name, Class<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
        context.addVariable(Variable.create(name, type, initialization));
        return this;
    }
    
    public VariableReferenceContextualStatementBuilder loadVariable(String name) {
        statement = context.getVariable(name);
        return ContextualStatementBuilderImpl.createInContextOf(this);
    }

    public ContextualStatementBuilder loadLiteral(Object o) {
        statement = LiteralFactory.getLiteral(o);
        return ContextualStatementBuilderImpl.createInContextOf(this);
    }

    public ContextualStatementBuilder load(Object o) {
        statement = GenUtil.generate(context, o);
        return ContextualStatementBuilderImpl.createInContextOf(this);
    }

    public ObjectBuilder newObject(MetaClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(JavaReflectionClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(Class<?> type) {
        return ObjectBuilder.newInstanceOf(type);
    }
}