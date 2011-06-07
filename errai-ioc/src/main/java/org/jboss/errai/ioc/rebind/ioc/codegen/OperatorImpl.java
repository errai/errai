package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidExpressionException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OperatorImpl implements Operator {

    private final String canonicalString;
    private final int operatorPrecedence;
    private final MetaClass[] constraints;

    OperatorImpl(String canonicalString, int operatorPrecedence, Class<?>... constraints) {
        this.canonicalString = canonicalString;
        this.operatorPrecedence = operatorPrecedence;
        this.constraints = MetaClassFactory.fromClassArray(constraints);
    }

    public String getCanonicalString() {
        return canonicalString;
    }

    public int getOperatorPrecedence() {
        return operatorPrecedence;
    }

    public boolean isHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(Operator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }


    public void assertCanBeApplied(MetaClass clazz) {
        if (!canBeApplied(clazz)) {
            throw new InvalidExpressionException("Not a valid type for operator '" +
                    canonicalString + "':" + clazz.getFullyQualifedName());
        }
    }

    public boolean canBeApplied(MetaClass clazz) {
        if (constraints.length == 0) return true;

        for (MetaClass mc : constraints) {
            if (mc.isAssignableFrom(clazz)) return true;
        }

        return false;
    }
}