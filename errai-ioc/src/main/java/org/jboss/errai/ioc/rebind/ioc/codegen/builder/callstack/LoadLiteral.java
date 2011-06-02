package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LoadLiteral extends AbstractCallElement {
    private Object literalValue;

    public LoadLiteral(Object literalValue) {
        this.literalValue = literalValue;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {
        nextOrReturn(writer, context, LiteralFactory.getLiteral(literalValue));
    }
}
