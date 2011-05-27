package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
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
        context.setStatement(context.getVariable(name));
        return ContextualStatementBuilder.createInContextOf(this);
    }
    
    public ContextualStatementBuilder loadLiteral(Object o) {
        context.setStatement(LiteralFactory.getLiteral(o));
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
