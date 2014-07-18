package org.jboss.errai.ui.rebind.less;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

/**
 * Compiles and optimizes LESS/CSS files. Compiled stylesheets are associated with
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author edewit@redhat.com
 */
public class LessStylesheetContext {
  private Set<StylesheetOptimizer> optimizedStylesheets = new LinkedHashSet<StylesheetOptimizer>();
  private final TreeLogger logger;
  private final PropertyOracle oracle;

  public LessStylesheetContext(TreeLogger logger, PropertyOracle oracle) {
    this.oracle = oracle;
    this.logger = logger;
  }

  /**
   * Compile and optimize LESS/CSS stylsheets.
   *
   * @param stylesheets URLs to the stylesheets to be compiled and optimized.
   */
  public void compileLessStylesheets(final Collection<URL> stylesheets) throws IOException {
    for (final URL stylesheet : stylesheets) {
      compileLessStylesheet(stylesheet);
    }
  }

  private void compileLessStylesheet(final URL stylesheet) throws IOException {
    final File cssFile = convertToCss(stylesheet);
    final StylesheetOptimizer stylesheetOptimizer = optimize(cssFile);
    optimizedStylesheets.add(stylesheetOptimizer);
  }

  private File convertToCss(URL resource) throws IOException {
    return new LessConverter(logger, oracle).convert(resource);
  }

  private StylesheetOptimizer optimize(File cssFile) {
    final StylesheetOptimizer stylesheetOptimizer;
    try {
      stylesheetOptimizer = new StylesheetOptimizer(cssFile);
    } catch (UnableToCompleteException e) {
      throw new RuntimeException("could not parse/optimize less stylesheet", e);
    }
    return stylesheetOptimizer;
  }

  public Map<String, String> getStyleMapping() {
    Map<String, String> styleMapping = new HashMap<String, String>();
    for (StylesheetOptimizer stylesheet : optimizedStylesheets) {
      styleMapping.putAll(stylesheet.getConvertedSelectors());
    }
    return styleMapping;
  }

  public String getStylesheet() {
    StringBuilder sb = new StringBuilder();
    for (StylesheetOptimizer stylesheet : optimizedStylesheets) {
      sb.append(stylesheet.output());
    }
    return sb.toString();
  }
}
