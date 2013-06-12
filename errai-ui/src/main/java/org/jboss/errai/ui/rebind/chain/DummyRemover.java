package org.jboss.errai.ui.rebind.chain;

import org.jboss.errai.ui.shared.chain.Command;
import org.jboss.errai.ui.shared.chain.Context;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.jboss.errai.ui.rebind.chain.TemplateCatalog.RESULT;

/**
 * @author edewit@redhat.com
 */
public class DummyRemover implements Command {

  @Override
  public void execute(Context context) {
    Element element = (Element) context.get(TemplateCatalog.ELEMENT);
    String dummy = element.getAttribute("data-role");

    if (dummy != null && "dummy".equals(dummy)) {
      final NodeList childNodes = element.getChildNodes();
      for (int i = 0, len = childNodes.getLength(); i < len; i++) {
        element.removeChild(childNodes.item(0));
      }

      context.put(RESULT, element.getOwnerDocument());
    }
  }

  @Override
  public Context createInitialContext() {
    return new Context();
  }
}
