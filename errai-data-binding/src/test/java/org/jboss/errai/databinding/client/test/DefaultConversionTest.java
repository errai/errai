package org.jboss.errai.databinding.client.test;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.jboss.errai.databinding.client.api.Convert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the built-in default conversions in {@link Convert}.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class DefaultConversionTest {

  @Test
  public void testStringToString() throws Exception {
    Object result = Convert.to(String.class, "test");
    assertEquals("test", result);
  }

  @Test
  public void testNullStringBecomesEmpty() throws Exception {
    Object result = Convert.to(String.class, null);
    assertEquals("", result);
  }

  @Test
  public void testStringToInt() throws Exception {
    Object result = Convert.to(Integer.class, "1234");
    assertEquals(Integer.valueOf(1234), result);
  }

  @Test
  public void testIntToString() throws Exception {
    Object result = Convert.to(String.class, 1234);
    assertEquals("1234", result);
  }

  @Test
  public void testStringToLong() throws Exception {
    Object result = Convert.to(Long.class, "1234");
    assertEquals(Long.valueOf(1234), result);
  }

  @Test
  public void testLongToString() throws Exception {
    Object result = Convert.to(String.class, 1234L);
    assertEquals("1234", result);
  }

  @Test
  public void testStringToFloat() throws Exception {
    Object result = Convert.to(Float.class, "1234");
    assertEquals(Float.valueOf(1234), result);
  }

  @Test
  public void testFloatToString() throws Exception {
    Object result = Convert.to(String.class, 1234.5f);
    assertEquals("1234.5", result);
  }

  @Test
  public void testStringToDouble() throws Exception {
    Object result = Convert.to(Double.class, "1234");
    assertEquals(Double.valueOf(1234), result);
  }

  @Test
  public void testDoubleToString() throws Exception {
    Object result = Convert.to(String.class, 1234.5d);
    assertEquals("1234.5", result);
  }

  @Test
  public void testStringToBooleanTrue() throws Exception {
    Object result = Convert.to(Boolean.class, "true");
    assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void testBooleanTrueToString() throws Exception {
    Object result = Convert.to(String.class, true);
    assertEquals("true", result);
  }

  @Test
  public void testStringToBooleanFalse() throws Exception {
    Object result = Convert.to(Boolean.class, "false");
    assertEquals(Boolean.FALSE, result);
  }

  @Test
  public void testBooleanFalseToString() throws Exception {
    Object result = Convert.to(String.class, false);
    assertEquals("false", result);
  }

  @Test
  public void testBigDecimalToString() throws Exception {
    BigDecimal bd = new BigDecimal(System.currentTimeMillis());
    Object result = Convert.to(String.class, bd);
    assertEquals(bd.toString(), result);
  }
  
  @Test
  public void testStringToBigDecimal() throws Exception {
    BigDecimal bd = new BigDecimal(System.currentTimeMillis());
    Object result = Convert.to(BigDecimal.class, bd.toString());
    assertEquals(bd, result);
  }
  
  @Test
  public void testBigIntegerToString() throws Exception {
    BigInteger bn = new BigInteger(String.valueOf(System.currentTimeMillis()));
    Object result = Convert.to(String.class, bn);
    assertEquals(bn.toString(), result);
  }
  
  @Test
  public void testStringToBigInteger() throws Exception {
    BigInteger bn = new BigInteger(String.valueOf(System.currentTimeMillis()));
    Object result = Convert.to(BigInteger.class, bn.toString());
    assertEquals(bn, result);
  }
  
  // ignoring this test because converting dates leads to a GWT.create() call and this is not a GWTTestCase.
  @Ignore @Test
  public void testDateRoundTrip() throws Exception {
    @SuppressWarnings("deprecation")
    Date d = new Date(123, 5, 6);

    Object result = Convert.to(Date.class, Convert.to(String.class, d));

    assertEquals(d, result);
  }

}
