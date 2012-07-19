package org.jboss.errai.codegen;

import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock
 */
public enum BitwiseOperator implements Operator {
  And("&", 6),
  Or("|", 4),
  Xor("^", 5),
  ShiftRight(">>", 9),
  UnsignedShiftRight(">>>", 9),
  ShiftLeft("<<", 9);

  private final Operator operator;

  BitwiseOperator(final String canonicalString, final int operatorPrecedence) {
    operator = new OperatorImpl(canonicalString, operatorPrecedence);
  }

  @Override
  public String getCanonicalString() {
    return operator.getCanonicalString();
  }

  @Override
  public int getOperatorPrecedence() {
    return operator.getOperatorPrecedence();
  }

  @Override
  public boolean isHigherPrecedenceThan(final Operator operator) {
    return operator.getOperatorPrecedence() < getOperatorPrecedence();
  }

  @Override
  public boolean isEqualOrHigherPrecedenceThan(final Operator operator) {
    return operator.getOperatorPrecedence() <= getOperatorPrecedence();
  }

  @Override
  public boolean canBeApplied(final MetaClass clazz) {
    return operator.canBeApplied(clazz);
  }

  @Override
  public void assertCanBeApplied(final MetaClass clazz) {
    operator.assertCanBeApplied(clazz);
  }
}
