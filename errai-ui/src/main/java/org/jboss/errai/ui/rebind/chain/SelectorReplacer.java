package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacer implements Command {

  public static final String MAPPING = "mapping";

  @Override
  @SuppressWarnings("unchecked")
  public void execute(Context context) {
    Map<String, String> selectorMap = (Map<String, String>) context.get(MAPPING);
    Element element = (Element) context.get(TemplateCatalog.ELEMENT);
    String selector = element.getAttribute("class");
    if (selector != null && selectorMap != null) {
      final String[] classSelectors = selector.split(" ");
      for (String classSelector : classSelectors) {
        final String obfuscatedSelector = selectorMap.get(classSelector);
        if (obfuscatedSelector != null) {
          selector = selector.replaceAll(classSelector, obfuscatedSelector);
        }
      }
      element.setAttribute("class", selector);
    }
  }
}
