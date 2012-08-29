package org.jboss.errai.codegen;

import org.jboss.errai.codegen.meta.MetaClass;

/**
 * @author Mike Brock
 */
public class TernaryStatement extends AbstractStatement {
  private final BooleanExpression condition;
  private final Statement trueStatement;
  private final Statement falseStatement;
  private MetaClass returnType;

  public TernaryStatement(final BooleanExpression condition,
                          final Statement trueStatement,
                          final Statement falseStatement) {
    this.condition = condition;
    this.trueStatement = trueStatement;
    this.falseStatement = falseStatement;
  }

  @Override
  public String generate(final Context context) {
    final String conditionString = condition.generate(context);
    final String trueString = trueStatement.generate(context);
    final String falseString = falseStatement.generate(context);

    returnType = trueStatement.getType();
    if (falseStatement.getType().isAssignableFrom(returnType)) {
      returnType = falseStatement.getType();
    }

    return conditionString.concat(" ? ").concat(trueString).concat(" : ").concat(falseString);
  }

  @Override
  public MetaClass getType() {
    return returnType;
  }
}
