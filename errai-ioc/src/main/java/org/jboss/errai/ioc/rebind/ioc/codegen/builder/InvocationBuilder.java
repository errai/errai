package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * StatementBuilder to generate method invocations.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilder extends AbstractStatementBuilder {
    private Statement invocationTarget;
    private MetaMethod method;
    private CallParameters parameters;

    protected InvocationBuilder(Context scope) {
        super(scope);
    }

    public static InvocationBuilder createInScopeOf(Statement parent) {
        return new InvocationBuilder(Context.create(parent.getContext()));
    }

    public ContextualStatementBuilder invoke(String methodName, Statement... parameters) {
        this.invocationTarget = context.getStatement();
        this.parameters = CallParameters.fromStatements(parameters);

        MetaClass[] parameterTypes = new MetaClass[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getType();
        }

        this.method = invocationTarget.getType().getDeclaredMethod(methodName, parameterTypes);
        if (method == null)
            throw new UndefinedMethodException(methodName, parameterTypes);

        buf.append(invocationTarget.generate()).append(".").append(method.getName()).append(this.parameters.generate());
        context.setStatement(this);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatementBuilder invoke(String methodName, Variable... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Variable parameter : parameters) {
            statements[i++] = context.getVariable(parameter.getName());
        }
        return invoke(methodName, statements);
    }
    
    public ContextualStatementBuilder invoke(String methodName, Object... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object o : parameters) {
            statements[i] = LiteralFactory.getLiteral(parameters[i++]);
        }
        return invoke(methodName, statements);
    }

    public MetaClass getType() {
        return method.getReturnType();
    }
}