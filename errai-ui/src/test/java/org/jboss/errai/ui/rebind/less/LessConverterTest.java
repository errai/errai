package org.jboss.errai.ui.rebind.less;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import static junit.framework.Assert.*;

/**
 * @author edewit@redhat.com
 */
public class LessConverterTest {

  @Test
  public void shouldConvertLessToNormalCss() throws IOException {
    // given
    final URL resource = getClass().getResource("/org/jboss/errai/package.less");

    // when
    final File css = new LessConverter().convert(resource);

    // then
    assertNotNull(css);
    assertTrue(css.exists());

    StringBuilder buffer = new StringBuilder();
    final Scanner scanner = new Scanner(css).useDelimiter("\n");
    while (scanner.hasNext()) {
      buffer.append(scanner.next());
    }
    assertEquals("#header {  color: #4d926f;}", buffer.toString());
  }

  @Test
  public void shouldTrowException() throws IOException {
    // given
    File lessError = File.createTempFile("error", ".less");
    final PrintWriter writer = new PrintWriter(lessError);
    writer.println(".header { color: @color; }");

    writer.close();

    // when
    try {
      new LessConverter().convert(lessError.toURI().toURL());
      fail("exception should have been thrown, because color is not defined");
    } catch (IOException e) {

      // then
      assertTrue(e.getMessage().contains("compile"));
    }

  }
}
