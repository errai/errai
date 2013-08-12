package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.rebind.less.LessStyleGenerator;
import org.jboss.errai.ui.shared.chain.Command;
import org.w3c.dom.Element;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacer implements Command {

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Element element) {
    Map<String, String> selectorMap = getStyleMapping();
    String selector = element.getAttribute("class");
    if (isNotEmpty(selector)) {
      final String[] classSelectors = selector.split(" ");
      for (String classSelector : classSelectors) {
        final String obfuscatedSelector = selectorMap.get(classSelector);
        if (obfuscatedSelector != null) {
          element.setAttribute("class", selector.replaceAll(classSelector, obfuscatedSelector));
        }
      }
    }
  }

  protected Map<String, String> getStyleMapping() {
    return LessStyleGenerator.getStyleMapping();
  }
}
