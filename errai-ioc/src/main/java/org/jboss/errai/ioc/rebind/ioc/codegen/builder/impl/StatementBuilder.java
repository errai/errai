package org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.StatementBegin;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableReferenceContextualStatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

import javax.enterprise.util.TypeLiteral;

/**
 * The root of our fluent StatementBuilder API.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock <cbrock@redhat.com>
 */
public class StatementBuilder extends AbstractStatementBuilder implements StatementBegin {

    public StatementBuilder(Context context) {
        super(context);

        if (context != null) {
            for (Variable v : context.getDeclaredVariables()) {
                appendCallElement(new DeclareVariable(v));
            }
        }
    }

    public static StatementBegin create() {
        return new StatementBuilder(null);
    }

    public static StatementBegin create(Context context) {
        return new StatementBuilder(context);
    }

    public StatementBuilder addVariable(String name, Class<?> type) {
        Variable v = Variable.create(name, type);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type) {
        Variable v = Variable.create(name, type);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, Object initialization) {
        Variable v = Variable.create(name, initialization);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, Class<?> type, Object initialization) {
        Variable v = Variable.create(name, type, initialization);
        return addVariable(v);
    }

    public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization) {
        Variable v = Variable.create(name, type, initialization);
        return addVariable(v);
    }

    private StatementBuilder addVariable(Variable v) {
        appendCallElement(new DeclareVariable(v));
        return this;
    }

    public VariableReferenceContextualStatementBuilder loadVariable(String name) {
        appendCallElement(new LoadVariable(name));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder loadLiteral(Object o) {
        appendCallElement(new LoadLiteral(o));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder load(Object o) {
        appendCallElement(new DynamicLoad(o));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
    }

    public ContextualStatementBuilder loadStatic(Class<?> clazz) {
        appendCallElement(new StaticLoad(clazz));
        return new ContextualStatementBuilderImpl(context, callElementBuilder);
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