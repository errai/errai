package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

/**
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

    public StatementBuilder addVariable(Variable variable) {
        context.addVariable(variable);
        return this;
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
    
    public VariableReferenceContextualStatement loadVariable(String name) {
        statement = context.getVariable(name);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatement loadLiteral(Object o) {
        statement = LiteralFactory.getLiteral(o);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatement load(Object o) {
        statement = GenUtil.generate(context, o);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ObjectBuilder newObject(MetaClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(JavaReflectionClass type) {
        return ObjectBuilder.newInstanceOf(type);
    }

    public ObjectBuilder newObject(Class<?> cls) {
        return ObjectBuilder.newInstanceOf(cls);
    }
}