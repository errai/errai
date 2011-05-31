package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.GenUtil;
import org.jboss.errai.ioc.rebind.ioc.codegen.MethodInvocation;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * StatementBuilder to generate method invocations.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InvocationBuilder extends AbstractStatementBuilder {

    protected InvocationBuilder(AbstractStatementBuilder parent) {
        super(Context.create(parent.context));
        this.parent = parent;
    }

    public static InvocationBuilder create(AbstractStatementBuilder parent) {
        return new InvocationBuilder(parent);
    }

    public ContextualStatement invoke(String methodName, Statement... parameters) {
        CallParameters callParams = CallParameters.fromStatements(parameters);

        MetaClass[] parameterTypes = callParams.getParameterTypes();
        MetaMethod method = parent.statement.getType().getDeclaredMethod(methodName, parameterTypes);
        if (method == null)
            throw new UndefinedMethodException(methodName, parameterTypes);

        statement = new MethodInvocation(parent.statement, method, callParams);
        return ContextualStatementBuilder.createInContextOf(this);
    }

    public ContextualStatement invoke(String methodName, Object... parameters) {
        return invoke(methodName, GenUtil.generateCallParameters(context, parameters));
    }
}