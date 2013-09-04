package org.jboss.errai.ui.rebind.less;

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
  private static LessStylesheetContext instance;

  private static final Object lock = new Object();

  private LessStylesheetContext() {
    init();
  }

  public static LessStylesheetContext getInstance() {
    if (instance == null) {
      synchronized (lock) {

        if (instance == null) {
          instance = new LessStylesheetContext();
        }
      }
    }

    return instance;
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
      cssFile = new LessConverter().convert(resource);
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

  public Set<StylesheetOptimizer> getOptimizedStylesheets() {
    return new HashSet<StylesheetOptimizer>(optimizedStylesheets);
  }

  public Map<String, String> getStyleMapping() {
    Map<String, String> styleMapping = new HashMap<String, String>();
    for (StylesheetOptimizer stylesheet : getOptimizedStylesheets()) {
      styleMapping.putAll(stylesheet.getConvertedSelectors());
    }
    return styleMapping;
  }
}
