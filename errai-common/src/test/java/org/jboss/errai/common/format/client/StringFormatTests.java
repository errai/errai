/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.common.format.client;

import java.util.Date;

import org.jboss.errai.common.client.logging.util.StringFormat;
import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests for the {@link StringFormat} class.
 * 
 * Most of these tests verify behaviour identical to
 * {@link String#format(String, Object...)}, but in some cases the output
 * differs slightly for the convenience of using pre-existing GWT
 * implementations.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SuppressWarnings("deprecation")
public class StringFormatTests extends GWTTestCase {

  @Test
  public void testScientificNotationLower() throws Exception {
    assertEquals("3.141593e00", StringFormat.format("%e", Math.PI));
  }

  @Test
  public void testScientificNotationUpper() throws Exception {
    assertEquals("3.141593E00", StringFormat.format("%E", Math.PI));
  }

  @Test
  public void testScientificNotationPrecision() throws Exception {
    assertEquals("3.14E00", StringFormat.format("%.2E", Math.PI));
  }

  @Test
  public void testGConversionLower() throws Exception {
    assertEquals(StringFormat.format("%e", Math.PI), StringFormat.format("%g", Math.PI));
  }

  @Test
  public void testGConversionUpper() throws Exception {
    assertEquals(StringFormat.format("%E", Math.PI), StringFormat.format("%G", Math.PI));
  }

  // @Test
  // public void testHexScientificNotationLower() throws Exception {
  // assertEquals("0x1.921fb54442d18p1", StringFormat.format("%a", Math.PI));
  // }
  //
  // @Test
  // public void testHexScientificNotationUpper() throws Exception {
  // assertEquals("0x1.921FB54442D18P1", StringFormat.format("%A", Math.PI));
  // }

  @Test
  public void testStringLower() throws Exception {
    assertEquals("value", StringFormat.format("%s", "value"));
  }

  @Test
  public void testStringUpper() throws Exception {
    assertEquals("VALUE", StringFormat.format("%S", "value"));
  }

  @Test
  public void testStringPrecision() throws Exception {
    String val = "123456789";
    assertEquals(val.substring(0, 3), StringFormat.format("%.3s", val));
  }

  @Test
  public void testStringWidth() throws Exception {
    String val = "123456789";
    assertEquals(" " + val, StringFormat.format("%10s", val));
  }

  @Test
  public void testStringNull() throws Exception {
    assertEquals("null", StringFormat.format("%s", null));
  }

  @Test
  public void testInt() throws Exception {
    assertEquals("1337", StringFormat.format("%d", 1337));
  }

  @Test
  public void testBooleanNonNull() throws Exception {
    assertEquals("true", StringFormat.format("%b", new Object()));
  }

  @Test
  public void testBooleanUpper() throws Exception {
    assertEquals("TRUE", StringFormat.format("%B", new Object()));
  }

  @Test
  public void testBooleanNullUpper() throws Exception {
    assertEquals("false", StringFormat.format("%b", null));
  }

  @Test
  public void testBooleanTrue() throws Exception {
    assertEquals("true", StringFormat.format("%b", true));
  }

  @Test
  public void testBooleanFalse() throws Exception {
    assertEquals("false", StringFormat.format("%b", false));
  }

  @Test
  public void testHexStringLower() throws Exception {
    Object obj = new Object() {
      @Override
      public int hashCode() {
        return 0xabcdef;
      }
    };
    assertEquals(Integer.toHexString(obj.hashCode()), StringFormat.format("%h", obj));
  }

  @Test
  public void testHexStringUpper() throws Exception {
    Object obj = new Object() {
      @Override
      public int hashCode() {
        return 0xabcdef;
      }
    };
    assertEquals(Integer.toHexString(obj.hashCode()).toUpperCase(), StringFormat.format("%H", obj));
  }

  @Test
  public void testHexStringNull() throws Exception {
    assertEquals("null", StringFormat.format("%h", null));
  }

  @Test
  public void testUnicodeCharacterLower() throws Exception {
    assertEquals("c", StringFormat.format("%c", (int) 'c'));
  }

  @Test
  public void testUnicodeCharacterUpper() throws Exception {
    assertEquals("C", StringFormat.format("%C", (int) 'c'));
  }

  @Test
  public void testOctal() throws Exception {
    assertEquals(Integer.toOctalString(735), StringFormat.format("%o", 735));
  }

  @Test
  public void testHexIntLower() throws Exception {
    assertEquals(Integer.toHexString(725815), StringFormat.format("%x", 725815));
  }

  @Test
  public void testHexIntUpper() throws Exception {
    assertEquals(Integer.toHexString(725815).toUpperCase(), StringFormat.format("%X", 725815));
  }

  @Test
  public void testFloatingPoint() throws Exception {
    assertEquals(String.valueOf(1.0), StringFormat.format("%f", 1.0));
  }

  @Test
  public void testLiteralPercent() throws Exception {
    assertEquals("%", StringFormat.format("%%"));
  }

  @Test
  public void testNewline() throws Exception {
    assertEquals("\n", StringFormat.format("%n"));
  }

  @Test
  public void testLongDate() throws Exception {
    long time = 1000000000;
    assertEquals("08:46", StringFormat.format("%tR", time));
  }

  @Test
  public void testDate() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("08:46", StringFormat.format("%tR", date));
  }

  @Test
  public void testDateUpperT() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("08:46:40", StringFormat.format("%tT", date));
  }

  @Test
  public void testDateLowerR() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("08:46:40 AM", StringFormat.format("%tr", date));
  }

  @Test
  public void testDateUpperD() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("01/12/70", StringFormat.format("%tD", date));
  }

  @Test
  public void testDateUpperF() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("1970-01-12", StringFormat.format("%tF", date));
  }

  @Test
  public void testDateLowerC() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("Mon Jan 12 08:46:40 UTC-5 1970", StringFormat.format("%tc", date));
  }

  @Test
  public void testDateLowerK() throws Exception {
    Date date = new Date(1000000000);
    date.setHours(13);
    assertEquals("13", StringFormat.format("%tk", date));
  }

  @Test
  public void testDateLowerL() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("8", StringFormat.format("%tl", date));
  }

  @Test
  public void testDateUpperL() throws Exception {
    Date date = new Date(10000000123L);
    assertEquals(String.valueOf(123), StringFormat.format("%tL", date));
  }

  @Test
  public void testDateUpperN() throws Exception {
    Date date = new Date(1000000123L);
    assertEquals("123000000", StringFormat.format("%tN", date));
  }

  @Test
  public void testDateLowerZ() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("-0500", StringFormat.format("%tz", date));
  }

  @Test
  public void testDateLowerS() throws Exception {
    Date date = new Date(1000000000);
    assertEquals(String.valueOf(1000000), StringFormat.format("%ts", date));
  }

  @Test
  public void testDateUpperQ() throws Exception {
    Date date = new Date(1000000000);
    assertEquals(String.valueOf(1000000000), StringFormat.format("%tQ", date));
  }

  @Test
  public void testDateUpperB() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("January", StringFormat.format("%tB", date));
  }

  @Test
  public void testDateUpperA() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("Monday", StringFormat.format("%tA", date));
  }

  @Test
  public void testDateUpperC() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("19", StringFormat.format("%tC", date));
  }

  @Test
  public void testDateLowerJ() throws Exception {
    Date date = new Date(1000000000);
    assertEquals("012", StringFormat.format("%tj", date));
  }

  @Test
  public void testDateLowerE() throws Exception {
    Date date = new Date(1000000000);
    date.setDate(9);
    assertEquals("9", StringFormat.format("%te", date));
  }

  @Test
  public void testMultipleConversions() throws Exception {
    String val1 = "happy";
    String val2 = "rainbows";
    String val3 = "bananas";

    assertEquals(val1 + " " + val2 + " " + val3, StringFormat.format("%s %s %s", val1, val2, val3));
  }

  @Test
  public void testIndexedConversions() throws Exception {
    String val1 = "happy";
    String val2 = "rainbows";
    String val3 = "bananas";

    assertEquals(val1 + " " + val2 + " " + val3, StringFormat.format("%3$s %1$s %2$s", val2, val3, val1));
  }

  @Test
  public void testIndexedAndNonIndexedConversions() throws Exception {
    String val1 = "happy";
    String val2 = "rainbows";
    String val3 = "bananas";

    assertEquals(val3 + " " + val1 + " " + val2 + " " + val1, StringFormat.format("%3$s %s %s %1$s", val1, val2, val3));
  }

  @Test
  public void testInvalidFlagsThrowsException() throws Exception {
    // Currently no flags are supported, so check that an exception is thrown
    // when any is given
    try {
      StringFormat.format("%-s", "test");
      fail("An exception should be thrown.");
    }
    catch (Exception e) {
    }
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.common.format.StringFormatTests";
  }

}
