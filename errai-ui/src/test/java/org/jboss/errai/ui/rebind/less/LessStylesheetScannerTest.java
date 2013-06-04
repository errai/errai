package org.jboss.errai.ui.rebind.less;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author edewit@redhat.com
 */
public class LessStylesheetScannerTest {

  @Test
  public void shouldFindLessStylesheets() {
    // given
    LessStylesheetScanner scanner = new LessStylesheetScanner();

    // when
    Collection<String> resources = scanner.getLessResources();

    // then
    assertNotNull(resources);
    assertFalse(resources.isEmpty());
    assertEquals(2, resources.size());
    assertTrue(resources.contains("find.less"));

    for (String resource : resources) {
      assertNotNull(LessStylesheetScannerTest.class.getResource("/" + resource));
    }
  }
}
