package org.jboss.errai.ui.test.stylesheet;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.resources.css.GenerateCssAst;
import com.google.gwt.resources.css.ast.CssNode;
import com.google.gwt.resources.css.ast.CssStylesheet;
import junit.framework.TestCase;

import java.net.URL;

/**
 * @author edewit@redhat.com
 */
public class CssParseTest extends TestCase {

  public void testParseCss() throws Exception {

    final URL resource = getClass().getClassLoader().getResource("org/jboss/errai/ui/test/stylesheet/style.css");
    CssStylesheet stylesheet = GenerateCssAst.exec(TreeLogger.NULL, resource);

    System.out.println("stylesheet = " + stylesheet);
    System.out.println("stylesheet.getNodes() = " + stylesheet.getNodes());
    for (CssNode cssNode : stylesheet.getNodes()) {
      System.out.println("cssNode = " + cssNode);
    }


    assertNotNull(stylesheet);
  }
}
