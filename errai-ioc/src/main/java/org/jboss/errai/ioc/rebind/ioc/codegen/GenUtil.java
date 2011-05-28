package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GenUtil {
    public static Statement[] generateCallParameters(Context context, Object... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object o : parameters) {
            if (o instanceof Reference) {
                statements[i++] = context.getVariable(((Reference) o).getName());
            } else if (o instanceof Variable) {
                Variable v = (Variable) o;
                if (context.isScoped(v)) {
                    statements[i++] = v;
                } else {
                    throw new OutOfScopeException("variable cannot be referenced from this scope: " + v.getName());
                }
            } else {
                statements[i++] = LiteralFactory.getLiteral(o);
            }
        }
        return statements;
    }
}
