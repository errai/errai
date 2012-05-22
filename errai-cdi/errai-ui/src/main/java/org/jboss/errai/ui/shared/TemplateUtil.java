package org.jboss.errai.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public final class TemplateUtil {
  private TemplateUtil() {
  }

  public static void composite(Composite component, String templateContents) {
    // do something
    System.out.println("Invoked " + TemplateUtil.class.getName() + ".composite(" + component + ")");

    final List<Element> dataFields = new ArrayList<Element>();

    final Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(templateContents);
    Element templateRoot = parserDiv.getFirstChildElement();

    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll

    Visit.accept(templateRoot, new Visitor() {
      @Override
      public void visit(VisitContext context, Element element) {
        if (element.hasAttribute("data-field")) {
          dataFields.add(element);
        }
      }
    });

    initWidget(component, new TemplateWidget(templateRoot));

    for (Element element : dataFields) {
      Element parentElement = element.getParentElement();
      System.out.println("Binding [data-field=" + element.getAttribute("data-field") + "]");

      // parentElement.replaceChild(label.getElement(), element);
    }
  }

  private static native void initWidget(Composite component, Widget root) /*-{
    component.@com.google.gwt.user.client.ui.Composite::initWidget(Lcom/google/gwt/user/client/ui/Widget;)(root);
  }-*/;
}
