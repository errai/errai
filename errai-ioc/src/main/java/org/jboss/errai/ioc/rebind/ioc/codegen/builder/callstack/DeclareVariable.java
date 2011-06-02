package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.Variable;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class DeclareVariable extends AbstractCallElement {
    private Variable variable;

    public DeclareVariable(Variable variable) {
        this.variable = variable;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        context.addVariable(variable);
        nextOrReturn(writer, context, null);
    }
}
