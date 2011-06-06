package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public enum UnaryOperator implements Operator {
    New("new", 0),
    Increment("++", 0),
    Decrement("--", 0);

    private final Operator operator;

    UnaryOperator(String canonicalString, int operatorPrecedence) {
        operator = new OperatorImpl(canonicalString, operatorPrecedence);
    }

    public String getCanonicalString() {
        return operator.getCanonicalString();
    }

    public int getOperatorPrecedence() {
        return operator.getOperatorPrecedence();
    }

    public boolean isHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }

    public boolean canBeApplied(MetaClass clazz) {
        return operator.canBeApplied(clazz);
    }

    public void assertCanBeApplied(MetaClass clazz) {
        operator.assertCanBeApplied(clazz);
    }
}
