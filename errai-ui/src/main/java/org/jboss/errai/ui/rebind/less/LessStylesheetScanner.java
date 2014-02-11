package org.jboss.errai.ui.rebind.less;

import java.util.Collection;

import org.jboss.errai.common.metadata.MetaDataScanner;
import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.ResourcesScanner;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.FilterBuilder;

/**
 * Find all less stylesheets on the classpath.
 * @author edewit@redhat.com
 */
public class LessStylesheetScanner {

  private final LessReflections reflections;

  public LessStylesheetScanner() {
    reflections = new LessReflections();
  }

  public Collection<String> getLessResources() {
    return reflections.getStore().get(LessResourceScanner.class).values();
  }

  private static class LessReflections extends Reflections {
    private LessReflections() {
      super(new ConfigurationBuilder()
              .filterInputsBy(new FilterBuilder().include(".*\\.less"))
              .setScanners(new LessResourceScanner())
              .setUrls(MetaDataScanner.getConfigUrls()));
      scan();
    }
  }

  private static class LessResourceScanner extends ResourcesScanner {
    @Override
    public boolean acceptsInput(String file) {
      return file != null && file.endsWith("less");
    }
  }
}
