package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.resources.css.ast.CssStylesheet;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    assertEquals(
            ".E17l0oxA  {\n" +
                    "  background : greenyellow;\n" +
                    "  margin-top : 3px;\n" +
                    "}\n" +
                    ".E17l0oxB , .E17l0oxC  {\n" +
                    "  background : greenyellow;\n" +
                    "}\n" +
                    "div.E17l0oxD  {\n" +
                    "  text-decoration : #000;\n" +
                    "}\n" +
                    "div.E17l0oxE .E17l0oxF  {\n" +
                    "  text-emphasis : #000;\n" +
                    "}\n" +
                    "div.E17l0oxG .E17l0oxH  {\n" +
                    "  background : #fafafa;\n" +
                    "}\n" +
                    "div.E17l0oxG div.E17l0oxI  {\n" +
                    "  color : #999;\n" +
                    "}\n" +
                    "div.E17l0oxG .E17l0oxF , div.E17l0oxE .E17l0oxF  {\n" +
                    "  color : blue;\n" +
                    "}\n" +
                    "div h1 {\n" +
                    "  border-left : 1px;\n" +
                    "}\n" +
                    ".E17l0oxJ:hover:after {\n" +
                    "  display : block;\n" +
                    "}\n"
            , stylesheetOptimizer.output());
  }
}
