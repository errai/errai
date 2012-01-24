package org.jboss.errai.cdi.demo.stock.client.shared;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TickBuilderTest {

  private TickBuilder tb;

  @Before
  public void createTickBuilder() {
    tb = new TickBuilder("XXX");
  }

  @Test
  public void testFormat1Digit2Decimals() {
    tb.price(new BigDecimal("0.01"));
    Assert.assertEquals("0.01", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat3Digits10Decimals() {
    tb.price(new BigDecimal("0.0000000123"));
    Assert.assertEquals("0.0000000123", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat0With2Decimals() {
    tb.price(new BigDecimal("0.00"));
    Assert.assertEquals("0.00", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat2Digit2Decimals() {
    tb.price(new BigDecimal("0.12"));
    Assert.assertEquals("0.12", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat3Digits2Decimals() {
    tb.price(new BigDecimal("1.23"));
    Assert.assertEquals("1.23", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat1Digit2DecimalsNegative() {
    tb.price(new BigDecimal("-0.01"));
    Assert.assertEquals("-0.01", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat2Digit2DecimalsNegative() {
    tb.price(new BigDecimal("-0.12"));
    Assert.assertEquals("-0.12", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testFormat3Digits2DecimalsNegative() {
    tb.price(new BigDecimal("-1.23"));
    Assert.assertEquals("-1.23", tb.toTick().getFormattedPrice());
  }

  @Test
  public void testChangeNegative() {
    tb.change(new BigDecimal("-1.23"));
    Assert.assertEquals("-1.23", tb.toTick().getFormattedChange());
  }

  @Test
  public void testChangePositive() {
    tb.change(new BigDecimal("1.23"));
    Assert.assertEquals("+1.23", tb.toTick().getFormattedChange());
  }

}
