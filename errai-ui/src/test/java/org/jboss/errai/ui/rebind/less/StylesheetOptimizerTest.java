package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.resources.css.ast.*;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author edewit@redhat.com
 */
public class StylesheetOptimizerTest {

  @Test
  public void shouldOptimizeCssFile() throws URISyntaxException, UnableToCompleteException {
    // given
    File simpleCss = new File(getClass().getResource("/simple.css").toURI());
    final StylesheetOptimizer stylesheetOptimizer = new StylesheetOptimizer(simpleCss);

    // when
    final CssStylesheet optimized = stylesheetOptimizer.getStylesheet();

    // then
    assertNotNull(optimized);

    final Visitor visitor = new Visitor();
    visitor.accept(optimized);

    final List<String> selectors =
            asList("div.E17l0oxD", "div.E17l0oxE.E17l0oxF", ".E17l0oxA", ".E17l0oxC", ".E17l0oxB", "div.E17l0oxG.E17l0oxH");
    assertEquals(new HashSet<String>(selectors), visitor.getSelectors());
  }

  public static class Visitor extends CssVisitor {
    Set<String> selectors = new HashSet<String>();

    @Override
    public boolean visit(CssRule x, Context ctx) {
      for (CssSelector selector : x.getSelectors()) {
        selectors.add(selector.getSelector());
      }
      return super.visit(x, ctx);
    }

    public Set<String> getSelectors() {
      return selectors;
    }
  }
}
