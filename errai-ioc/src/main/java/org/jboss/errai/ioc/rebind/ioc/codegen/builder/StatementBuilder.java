package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import javax.enterprise.util.TypeLiteral;

import org.jboss.errai.ioc.rebind.ioc.codegen.MetaClassFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
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
    
    public ContextualStatementBuilder loadVariable(String name, Class type) {
        return loadVariable(name, MetaClassFactory.get(type));
    }

    public ContextualStatementBuilder loadVariable(String name, TypeLiteral literal) {
        return loadVariable(name, MetaClassFactory.get(literal));
    }

    public ContextualStatementBuilder loadVariable(String name, MetaClass type) {
        Variable var = Variable.get(name, type);
        assertInScope(var);
        context.push(var);
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
