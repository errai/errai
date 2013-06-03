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
 * @author edewit@redhat.com
 */
public class SelectorMinifyVisitor extends CssModVisitor {
  private static final char[] BASE32_CHARS = new char[] {
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
      if (selector.getSelector().startsWith(".")) {
        final String selectorName = selector.getSelector().substring(1);
        final String minified = minify(selectorName);
        convertedSelectors.put(selectorName, minified);
        selector.setSelector(minified);
      }
    }
    return false;
  }

  private String minify(String selectorName) {
    return getPrefix(selectorName) + makeIdent(count++);
  }

  private String getPrefix(String selectorName) {
    if (classPrefix == null) {
      Adler32 checksum = new Adler32();
      checksum.update(Util.getBytes(selectorName));
      classPrefix = ".E" + Long.toString(checksum.getValue(), Character.MAX_RADIX);
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
