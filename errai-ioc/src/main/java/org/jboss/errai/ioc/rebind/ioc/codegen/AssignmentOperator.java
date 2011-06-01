package org.jboss.errai.ioc.rebind.ioc.codegen;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public enum AssignmentOperator {
    Assignment("=", 0, Object.class),
    PreIncrementAssign("+=", 0, CharSequence.class, Number.class),
    PostIncrementAssign("=+", 0, CharSequence.class, Number.class),
    PreDecrementAssign("-=", 0, Number.class),
    PostDecrementAssign("=-", 0, Number.class);

    private final String canonicalString;
    private final int operatorPrecedence;
    private final MetaClass[] applicability;

    AssignmentOperator(String canonicalString, int operatorPrecedence, Class<?>... applicability) {
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

    public boolean isHigherPrecedenceThan(UnaryOperator operator) {
        return operator.getOperatorPrecedence() < getOperatorPrecedence();
    }

    public boolean isEqualOrHigherPrecedenceThan(UnaryOperator operator) {
        return operator.getOperatorPrecedence() <= getOperatorPrecedence();
    }

    public boolean canBeApplied(MetaClass clazz) {
        for (MetaClass mc : applicability) {
            if (mc.isAssignableFrom(clazz)) return true;
        }

        return false;
    }

    public String generate(VariableReference reference, Statement statement) {
        if (!canBeApplied(statement.getType())) {
            throw new RuntimeException("variable expected");
        }

        return reference.getName() + " " + getCanonicalString() + " " + statement.generate(Context.create());
    }
}
