package org.jboss.errai.ui.rebind.less;

import org.jboss.errai.reflections.Reflections;
import org.jboss.errai.reflections.scanners.ResourcesScanner;
import org.jboss.errai.reflections.util.ClasspathHelper;
import org.jboss.errai.reflections.util.ConfigurationBuilder;
import org.jboss.errai.reflections.util.FilterBuilder;

import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * Find all less stylesheets based of the baseClasses classpath.
 * @author edewit@redhat.com
 */
public class LessStylesheetScanner {

  private final LessReflections reflections;

  public LessStylesheetScanner(Class<?> baseClass) {
    reflections = new LessReflections(baseClass);
  }

  protected Collection<String> getLessResources() {
    return reflections.getStore().get(LessResourceScanner.class).values();
  }

  private static class LessReflections extends Reflections {
    private LessReflections(final Class<?> baseClass) {
      super(new ConfigurationBuilder()
              .filterInputsBy(new FilterBuilder().include(".*\\.less"))
              .setScanners(new LessResourceScanner())
              .setUrls(asList(ClasspathHelper.forClass(baseClass))));
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
