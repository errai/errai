package org.jboss.errai.ui.rebind.less;

import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Converts a less resource to a css file using the java LessCompiler wrapper.
 * @author edewit@redhat.com
 */
public class LessConverter {

  public File convert(URL resource) throws IOException {
    LessCompiler lessCompiler = new LessCompiler();
    try {
      final File compiled = File.createTempFile("compiled", ".css");
      lessCompiler.compile(new File(resource.toURI()), compiled);
      return compiled;
    } catch (LessException e) {
      throw new IOException("specified less stylesheet could not be compiled to css", e);
    } catch (URISyntaxException e) {
      throw new IOException("could not convert resource to file", e);
    }
  }
}
