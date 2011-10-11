package org.jboss.errai.cdi.demo.stock.client.shared;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TickBuilderTest {

  private TickBuilder tick;
  
  @Before
  public void createTickBuilder() {
    tick = new TickBuilder();
  }
  
  @Test
  public void testFormat1Digit2Decimals() {
    tick.setAsk(1);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("0.01", tick.getFormattedAsk());
  }

  @Test
  public void testFormat3Digits10Decimals() {
    tick.setAsk(123);
    tick.setDecimalPlaces(10);
    Assert.assertEquals("0.0000000123", tick.getFormattedAsk());
  }

  @Test
  public void testFormat0With2Decimals() {
    tick.setAsk(0);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("0.00", tick.getFormattedAsk());
  }

  @Test
  public void testFormat2Digit2Decimals() {
    tick.setAsk(12);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("0.12", tick.getFormattedAsk());
  }

  @Test
  public void testFormat3Digits2Decimals() {
    tick.setAsk(123);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("1.23", tick.getFormattedAsk());
  }

  @Test
  public void testFormat1Digit2DecimalsNegative() {
    tick.setAsk(-1);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("-0.01", tick.getFormattedAsk());
  }

  @Test
  public void testFormat2Digit2DecimalsNegative() {
    tick.setAsk(-12);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("-0.12", tick.getFormattedAsk());
  }

  @Test
  public void testFormat3Digits2DecimalsNegative() {
    tick.setAsk(-123);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("-1.23", tick.getFormattedAsk());
  }
  
  @Test
  public void testChangeNegative() {
    tick.setChange(-123);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("-1.23", tick.getFormattedChange());
  }

  @Test
  public void testChangePositive() {
    tick.setChange(123);
    tick.setDecimalPlaces(2);
    Assert.assertEquals("+1.23", tick.getFormattedChange());
  }

}
