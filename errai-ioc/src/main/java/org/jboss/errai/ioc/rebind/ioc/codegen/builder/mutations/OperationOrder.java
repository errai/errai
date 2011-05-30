package org.jboss.errai.ioc.rebind.ioc.codegen.builder.mutations;

import org.jboss.errai.ioc.rebind.ioc.codegen.Statement;
import org.jboss.errai.ioc.rebind.ioc.codegen.UnaryOperator;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum OperationOrder {
    Postfix {
        @Override
        public String render(UnaryOperator operator, Statement value) {
            return value.generate() + operator.getCanonicalString();
        }
    },
    Prefix {
        @Override
        public String render(UnaryOperator operator, Statement value) {
            return operator.getCanonicalString() + value.generate();
        }
    };

    public abstract String render(UnaryOperator operator, Statement value);
}
