package org.jboss.errai.ioc.rebind.ioc.codegen;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum AssignmentOperator {
    Assignment("=", 0),
    PreIncrementAssign("+=", 0),
    PostIncrementAssign("=+", 0),
    PreDecrementAssign("-=", 0),
    PostDecrementAssign("=-", 0);

    private final String canonicalString;
    private final int operatorPrecedence;

    AssignmentOperator(String canonicalString, int operatorPrecedence) {
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

    public String generate(VariableReference reference, Statement statement) {
        return reference.getName() + " " + getCanonicalString() + " " + statement.generate();
    }
}
