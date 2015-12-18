package org.jboss.errai.codegen.test;

import static org.junit.Assert.assertEquals;

import org.jboss.errai.codegen.ArithmeticExpression;
import org.jboss.errai.codegen.ArithmeticOperator;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Arith;
import org.junit.Test;

public class ArithmeticTest {

  @Test
  public void testWideningByteChar() throws Exception {
    ArithmeticExpression expr = Arith.expr((byte) 1, ArithmeticOperator.Addition, (char) 3);
    assertEquals(MetaClassFactory.get(int.class), expr.getType());
  }

  @Test
  public void testWideningCharByte() throws Exception {
    ArithmeticExpression expr = Arith.expr((char) 1, ArithmeticOperator.Addition, (byte) 3);
    assertEquals(MetaClassFactory.get(int.class), expr.getType());
  }

  @Test
  public void testWideningIntLong() throws Exception {
    ArithmeticExpression expr = Arith.expr(1, ArithmeticOperator.Addition, (long) 3);
    assertEquals(MetaClassFactory.get(long.class), expr.getType());
  }

  @Test
  public void testWideningLongInt() throws Exception {
    ArithmeticExpression expr = Arith.expr((long) 1, ArithmeticOperator.Addition, 3);
    assertEquals(MetaClassFactory.get(long.class), expr.getType());
  }

  @Test
  public void testDoubleDoubleMakesDouble() throws Exception {
    ArithmeticExpression expr = Arith.expr(1.0, ArithmeticOperator.Addition, 3.0);
    assertEquals(MetaClassFactory.get(double.class), expr.getType());
  }

  @Test
  public void testCharCharMakesInt() throws Exception {
    ArithmeticExpression expr = Arith.expr((char) 1, ArithmeticOperator.Addition, (char) 3);
    assertEquals(MetaClassFactory.get(int.class), expr.getType());
  }

}
