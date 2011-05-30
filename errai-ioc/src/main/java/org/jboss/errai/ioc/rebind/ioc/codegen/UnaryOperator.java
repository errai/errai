package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum UnaryOperator {
    New("new", 0),
    Increment("++", 0),
    Decrement("--", 0);

    private final String canonicalString;
    private final int operatorPrecedence;

    UnaryOperator(String canonicalString, int operatorPrecedence) {
        this.canonicalString = canonicalString;
        this.operatorPrecedence = operatorPrecedence;
    }

    public String getCanonicalString() {
        return canonicalString;
    }

    public int getOperatorPrecedence() {
        return operatorPrecedence;
    }

    public boolean isHigherPrecedenceThan(UnaryOperator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(UnaryOperator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }
}
