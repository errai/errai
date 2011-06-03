package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.*;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.impl.AssignmentBuilder;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class AssignVariable extends AbstractCallElement {
    private AssignmentOperator operator;
    private Object value;

    public AssignVariable(AssignmentOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        writer.reset();
        Statement s =
                new AssignmentBuilder(operator, (VariableReference) statement, GenUtil.generate(context, value));
        nextOrReturn(writer, context, s);
    }
}
