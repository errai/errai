package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public enum BooleanOperator {
    Or("||", 3, Boolean.class),
    And("&&", 4, Boolean.class),
    Equals("==", 8),
    NotEquals("!=", 8),
    InstanceOf("instanceof", 9, new Class[]{Object.class}, new Class[]{Class.class}),
    GreaterThanOrEqual(">=", 9, Number.class),
    GreaterThan(">", 9, Number.class),
    LessThanOrEqual("<=", 9, Number.class),
    LessThan("<", 9, Number.class);

    private final Operator operator;

    BooleanOperator(String canonicalString, int operatorPrecedence, Class<?>... lhsConstraints) {
        operator = new OperatorImpl(canonicalString, operatorPrecedence, lhsConstraints);
    }
    
    BooleanOperator(String canonicalString, int operatorPrecedence, Class<?>[] lhsConstraints, Class<?>[] rhsConstraints) {
        operator = new OperatorImpl(canonicalString, operatorPrecedence, lhsConstraints, rhsConstraints);
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
    
    public void canBeAppliedLhs(MetaClass clazz) {
       operator.canBeAppliedLhs(clazz);
    }
    
    public void canBeAppliedRhs(MetaClass clazz) {
        operator.canBeAppliedRhs(clazz);
     }
}
