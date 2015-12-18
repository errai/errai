package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Command;
import org.w3c.dom.Element;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author edewit@redhat.com
 */
public class SelectorReplacer implements Command {

  private final Map<String, String> styleSheetMapping;

  public SelectorReplacer(Map<String, String> styleSheetMapping) {
    this.styleSheetMapping = styleSheetMapping;
  }

  @Override
  public void execute(Element element) {
    String selector = element.getAttribute("class");
    if (isNotEmpty(selector)) {
      final String[] classSelectors = selector.split(" ");
      for (String classSelector : classSelectors) {
        final String obfuscatedSelector = styleSheetMapping.get(classSelector);
        if (obfuscatedSelector != null) {
          element.setAttribute("class", selector.replaceAll(classSelector, obfuscatedSelector));
        }
      }
    }
  }
}
