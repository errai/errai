package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * @author edewit@redhat.com
 */
public class LessStylesheetContext {
  private Set<StylesheetOptimizer> optimizedStylesheets = new HashSet<StylesheetOptimizer>();
  private final TreeLogger logger;
  private final PropertyOracle oracle;

  public LessStylesheetContext(TreeLogger logger, PropertyOracle oracle) {
    this.oracle = oracle;
    this.logger = logger;
    init();
  }

  private void init() {
    final Collection<String> lessStyles = new LessStylesheetScanner().getLessResources();
    for (String sheet : lessStyles) {
      final URL resource = LessStyleGenerator.class.getResource("/" + sheet);
      final File cssFile = convertToCss(resource);

      final StylesheetOptimizer stylesheetOptimizer = optimize(cssFile);
      optimizedStylesheets.add(stylesheetOptimizer);
    }
  }

  private File convertToCss(URL resource) {
    final File cssFile;
    try {
      cssFile = new LessConverter(logger, oracle).convert(resource);
    } catch (IOException e) {
      throw new RuntimeException("could not read less stylesheet", e);
    }
    return cssFile;
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
