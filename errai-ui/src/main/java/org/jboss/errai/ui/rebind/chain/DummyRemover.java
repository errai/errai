package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Command;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author edewit@redhat.com
 */
public class DummyRemover implements Command {

  @Override
  public void execute(Element element) {
    String dummy = element.getAttribute("data-role");

    if (dummy != null && "dummy".equals(dummy)) {
      final NodeList childNodes = element.getChildNodes();
      for (int i = 0, len = childNodes.getLength(); i < len; i++) {
        element.removeChild(childNodes.item(0));
      }
    }
  }
}
