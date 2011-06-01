package org.jboss.errai.ioc.rebind.ioc.codegen.builder.mutations;

import org.jboss.errai.ioc.rebind.ioc.codegen.Context;
import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.UnaryOperator;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum OperationOrder {
    Postfix {
        @Override
        public String render(UnaryOperator operator, Statement value, Context context) {
            return value.generate(context) + operator.getCanonicalString();
        }
    },
    Prefix {
        @Override
        public String render(UnaryOperator operator, Statement value, Context context) {
            return operator.getCanonicalString() + value.generate(context);
        }
    };

    public abstract String render(UnaryOperator operator, Statement value, Context context);
}
