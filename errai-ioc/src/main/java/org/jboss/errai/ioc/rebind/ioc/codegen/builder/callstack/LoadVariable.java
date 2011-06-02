package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.VariableReference;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LoadVariable extends AbstractCallElement {
    private String variableName;

    public LoadVariable(String variableName) {
        this.variableName = variableName;
    }

    public String getStatement(Context context, Statement statement) {
        VariableReference ref = context.getVariable(variableName);

        if (ref == null) {
            throw new OutOfScopeException(variableName);
        }

        return nextOrReturn(context, ref);
    }
}
