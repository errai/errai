package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.exception.InvalidOperatorException;
import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class OperatorImpl implements Operator {
   
    private final String canonicalString;
    private final int operatorPrecedence;
    private final MetaClass[] applicability;

    OperatorImpl(String canonicalString, int operatorPrecedence, Class<?>... applicability) {
        this.canonicalString = canonicalString;
        this.operatorPrecedence = operatorPrecedence;
        this.applicability = MetaClassFactory.fromClassArray(applicability);
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
    
    public void canBeApplied(MetaClass clazz) {
        for (MetaClass mc : applicability) {
           // if (GenUtil.isAssignable(mc, clazz)) return;
            if(mc.isAssignableFrom(clazz)) return;
        }

        throw new InvalidOperatorException(getCanonicalString() + 
                " not a valid operator for type:" + clazz.getFullyQualifedName());
    }
}
