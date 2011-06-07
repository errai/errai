package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.StatementBuilder;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.impl.java.JavaReflectionClass;

import javax.enterprise.util.TypeLiteral;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface StatementBegin {

    public StatementBuilder addVariable(String name, Class<?> type);

    public StatementBuilder addVariable(String name, TypeLiteral<?> type);

    public StatementBuilder addVariable(String name, Object initialization);

    public StatementBuilder addVariable(String name, Class<?> type, Object initialization);

    public StatementBuilder addVariable(String name, TypeLiteral<?> type, Object initialization);

    public VariableReferenceContextualStatementBuilder loadVariable(String name);

    public ContextualStatementBuilder loadLiteral(Object o);

    public ContextualStatementBuilder load(Object o);

    public ContextualStatementBuilder invokeStatic(Class<?> clazz, String methodName, Object... parameters);

    public ObjectBuilder newObject(MetaClass type);

    public ObjectBuilder newObject(JavaReflectionClass type);

    public ObjectBuilder newObject(Class<?> type);
}
