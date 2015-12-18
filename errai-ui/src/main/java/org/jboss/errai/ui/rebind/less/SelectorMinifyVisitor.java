package org.jboss.errai.ui.rebind.less;

import com.google.gwt.dev.util.Util;
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssModVisitor;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssSelector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Adler32;

/**
 * A CssModVisitor that will change the minifiy the class selectors, the code here is 'borrowed' from
 * {@link com.google.gwt.resources.rg.CssResourceGenerator}
 *
 * @author edewit@redhat.com
 */
public class SelectorMinifyVisitor extends CssModVisitor {
  private static final char[] BASE32_CHARS = new char[]{
          'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
          'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '-', '0',
          '1', '2', '3', '4'};

  private Map<String, String> convertedSelectors = new HashMap<String, String>();
  private String classPrefix;
  private int count;

  @Override
  public boolean visit(CssRule cssRule, Context context) {
    final List<CssSelector> cssRuleSelectors = cssRule.getSelectors();
    for (CssSelector selector : cssRuleSelectors) {
      if (selector.getSelector().contains(".")) {
        selector.setSelector(obfuscate(selector.getSelector()));
      }
    }
    return false;
  }

  private String obfuscate(String selector) {
    StringBuilder sb = new StringBuilder();
    final String[] descendants = selector.split(" ");
    for (String descendant : descendants) {
      final int index = descendant.indexOf(".");
      if (index != -1) {
        final String prefix = descendant.substring(0, index);
        sb.append(prefix);
        final String[] selectors = descendant.substring(index + 1).split("\\.");
        for (String className : selectors) {
          String rest = " ";
          final int j = className.indexOf(":");
          if (j != -1) {
            rest = className.substring(j);
            className = className.substring(0, j);
          }
          final String minified;
          if (convertedSelectors.containsKey(className)) {
            minified = convertedSelectors.get(className);
          } else {
            minified = minify(className);
            convertedSelectors.put(className, minified);
          }
          sb.append(".").append(minified).append(rest);
        }
      } else {
        sb.append(descendant).append(" ");
      }
    }
    return sb.toString();
  }

  private String minify(String selectorName) {
    return getPrefix(selectorName) + makeIdent(count++);
  }

  private String getPrefix(String selectorName) {
    if (classPrefix == null) {
      Adler32 checksum = new Adler32();
      checksum.update(Util.getBytes(selectorName));
      classPrefix = "E" + Long.toString(checksum.getValue(), Character.MAX_RADIX);
    }
    return classPrefix;
  }

  private static String makeIdent(long id) {
    assert id >= 0;
    StringBuilder b = new StringBuilder();

    // Use only guaranteed-alpha characters for the first character
    b.append(BASE32_CHARS[(int) (id & 0xf)]);
    id >>= 4;

    while (id != 0) {
      b.append(BASE32_CHARS[(int) (id & 0x1f)]);
      id >>= 5;
    }

    return b.toString();
  }

  public Map<String, String> getConvertedSelectors() {
    return convertedSelectors;
  }
}
