package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.VariableDeclarationBuilder.VariableInitializationBuilder;
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

    public ContextualStatementBuilder loadVariable(String name) {
        statement = context.getVariable(name);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatementBuilder loadLiteral(Object o) {
        statement = LiteralFactory.getLiteral(o);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatementBuilder load(Object o) {
        statement = GenUtil.generate(context, o);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public VariableInitializationBuilder declareVariable(String name, Class<?> type) {
        return VariableDeclarationBuilder.create(this).declareVariable(Variable.get(name, type));
    }
    
    public VariableInitializationBuilder declareVariable(String name, TypeLiteral<?> type) {
        return VariableDeclarationBuilder.create(this).declareVariable(Variable.get(name, type));
    }
    
    public VariableInitializationBuilder declareVariable(String name) {
        return VariableDeclarationBuilder.create(this).declareVariable(name);
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
