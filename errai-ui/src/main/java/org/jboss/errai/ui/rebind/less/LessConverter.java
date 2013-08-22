package org.jboss.errai.ui.rebind.less;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

/**
 * Converts a less resource to a css file using the java LessCompiler wrapper.
 * @author edewit@redhat.com
 */
public class LessConverter {

  public File convert(URL resource) throws IOException {
    LessCompiler lessCompiler = new LessCompiler();
    try {
      String lessFile = readLessFile(resource);
      final String css = lessCompiler.compile(lessFile);
      return createCssFile(css);
    } catch (LessException e) {
      throw new IOException("specified less stylesheet could not be compiled to css", e);
    }
  }

  private File createCssFile(String css) throws IOException {
    final File compiled = File.createTempFile("compiled", ".css");
    PrintWriter writer = new PrintWriter(compiled);
    writer.print(css);
    writer.close();
    return compiled;
  }

  private String readLessFile(URL resource) throws IOException {
    Scanner scanner = new Scanner(resource.openStream());
    String lessFile = "";
    while (scanner.hasNext()) lessFile += scanner.nextLine();
    return lessFile;
  }
}
