package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.builder.values.LiteralFactory;
import org.jboss.errai.ioc.rebind.ioc.codegen.exception.OutOfScopeException;
import org.mvel2.DataConversion;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class GenUtil {
    public static Statement[] generateCallParameters(Context context, Object... parameters) {
        Statement[] statements = new Statement[parameters.length];
        int i = 0;
        for (Object o : parameters) {
            statements[i++] = generate(context, o);
        }
        return statements;
    }

    public static Statement generate(Context context, Object o) {
        if (o instanceof Reference) {
            return context.getVariable(((Reference) o).getName());
        } else if (o instanceof Variable) {
            Variable v = (Variable) o;
            if (context.isScoped(v)) {
                return v;
            } else {
                throw new OutOfScopeException("variable cannot be referenced from this scope: " + v.getName());
            }
        } else if (o instanceof Statement) {
            return (Statement) o;
        } else {
            return LiteralFactory.getLiteral(o);
        }
    }

    public static Statement doInference(Context context, Object input, Class<?> targetType) {
        if (DataConversion.canConvert(targetType, input.getClass())) {
            return generate(context, DataConversion.convert(input, targetType));
        } else {
            throw new RuntimeException("cannot inference");
        }
    }
}
