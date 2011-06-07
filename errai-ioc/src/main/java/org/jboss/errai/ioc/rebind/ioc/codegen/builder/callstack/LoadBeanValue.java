package org.jboss.errai.ioc.rebind.ioc.codegen.builder.callstack;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.mvel2.util.PropertyTools;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class LoadBeanValue extends AbstractCallElement {
    private String beanExpression;

    public LoadBeanValue(String beanExpression) {
        this.beanExpression = beanExpression;
    }

    public void handleCall(CallWriter writer, Context context, Statement statement) {


    }

    private static void parseBeanExpr(String expression, Context context, Statement statement) {
        if (statement == null) {
            int idx = expression.indexOf(".");
            if (idx != -1) {
                //  load variable here or fail
            }
        }

        Class<?> returnType = statement.getType().asClass();
        for (String part : expression.split("\\.")) {
            PropertyTools.getFieldOrAccessor(returnType, part);
        }
    }

    private static Class<?> parsePart(String part, Context context, Class<?> partType) {
        return null;
    }
}
