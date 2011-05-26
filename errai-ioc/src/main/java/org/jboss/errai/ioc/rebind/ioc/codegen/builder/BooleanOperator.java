package org.jboss.errai.ioc.rebind.ioc.codegen.builder;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum BooleanOperator {
    Or("||", 3),
    And("&&", 4),
    Equals("==", 8),
    NotEquals("!=", 8),
    InstanceOf("instanceof", 9),
    GreaterThanOrEqual(">=", 9),
    GreaterThan(">", 9),
    LessThanOrEqual("<=", 9),
    LessThan("<", 9);

    private final String canonicalString;
    private final int operatorPrecedence;

    BooleanOperator(String canonicalString, int operatorPrecedence) {
        this.canonicalString = canonicalString;
        this.operatorPrecedence = operatorPrecedence;
    }

    public String getCanonicalString() {
        return canonicalString;
    }

    public int getOperatorPrecedence() {
        return operatorPrecedence;
    }

    public boolean isHigherPrecedenceThan(BooleanOperator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(BooleanOperator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }
}
