package org.jboss.errai.ui.nav.rebind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.junit.Test;


public class PathTemplateParseTest {

  private static List<String> tryParse(String template) {
    return NavigationGraphGenerator.parsePageUriTemplate(
            MetaClassFactory.get(PathTemplateParseTest.class), template);
  }

  @Test
  public void testEmptyTemplate() {
    List<String> l = tryParse("");
    assertEquals(1, l.size());
    assertEquals("PathTemplateParseTest", l.get(0));
  }

  @Test
  public void testTemplateWithOnlyParams() {
    List<String> l = tryParse("{param_one}");
    assertEquals(2, l.size());
    assertEquals("PathTemplateParseTest", l.get(0));
    assertEquals("param_one", l.get(1));
  }

  @Test
  public void testTemplateWithNamelessParam() {
    try {
      tryParse("page-name/{}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("nameless parameter"));
    }
  }

  @Test
  public void testTemplateWithNameAndParams() {
    List<String> l = tryParse("page-name/{p1}/{parameter2}");
    assertEquals(3, l.size());
    assertEquals("page-name", l.get(0));
    assertEquals("p1", l.get(1));
    assertEquals("parameter2", l.get(2));
  }

  @Test
  public void testTemplateWithJunkBetweenParams() {
    try {
      tryParse("page-name/x{p1}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("outside a parameter"));
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("'x'"));
    }
  }

  @Test
  public void testTemplateWithUnterminatedParam() {
    try {
      tryParse("page-name/{p1}/{parameter2");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("unterminated parameter"));
    }
  }

  @Test
  public void testTemplateWithDoubleTerminatedParam() {
    try {
      tryParse("page-name/{p1}}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("Found '}'"));
    }
  }

  @Test
  public void testTemplateWithDoubleOpenParam() {
    try {
      tryParse("page-name/{{p1}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("Found '{'"));
    }
  }

  @Test
  public void testTemplateWithInvalidFirstParamNameChar() {
    try {
      tryParse("page-name/{1p1}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("invalid Java identifier character '1'"));
    }
  }

  @Test
  public void testTemplateWithInvalidParamNamePartChar() {
    try {
      tryParse("page-name/{p.1}/{parameter2}");
      fail("No error for invalid template");
    } catch (IllegalArgumentException ex) {
      assertTrue("Unexpected exception message " + ex.getMessage(),
              ex.getMessage().contains("invalid Java identifier character '.'"));
    }
  }

}
