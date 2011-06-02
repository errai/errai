package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.CallParameters;
import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.MethodInvocation;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.UndefinedMethodException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaMethod;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MethodCall extends AbstractCallElement {
    private String methodName;
    private Statement[] parameters;

    public MethodCall(String methodName, Statement[] parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getStatement(Context context, Statement statement) {
        CallParameters callParams = CallParameters.fromStatements(parameters);

        MetaClass[] parameterTypes = callParams.getParameterTypes();
        MetaMethod method = statement.getType().getBestMatchingMethod(methodName, parameterTypes);
        if (method == null)
            throw new UndefinedMethodException(methodName, parameterTypes);

        statement = new MethodInvocation(method, callParams);

        return nextOrReturn(context, statement);
    }
}
