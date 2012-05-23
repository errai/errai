package org.jboss.errai.ui.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public final class TemplateUtil {
  private TemplateUtil() {
  }

  /**
   * Replace the {@link Element} with thte data-field of the given
   * {@link String} with the root {@link Element} of the given {@link UIObject}
   */
  public static void compositeComponentReplace(UIObject field, final Map<String, Element> dataFieldElements,
          String fieldName) {
    System.out.println("Compositing @Replace [data-field=" + fieldName + "]");
    Element element = dataFieldElements.get(fieldName);
    Element parentElement = element.getParentElement();
    parentElement.replaceChild(field.getElement(), element);
  }

  /**
   * Insert the root {@link Element} of the given {@link UIObject} into the
   * {@link Element} with the data-field of the given {@link String}
   */
  public static void compositeComponentInsert(UIObject field, final Map<String, Element> dataFieldElements,
          String fieldName) {
    System.out.println("Compositing @Insert [data-field=" + fieldName + "]");
    Element element = dataFieldElements.get(fieldName);
    element.setInnerHTML("");
    element.appendChild(field.getElement());
  }

  public static Element getRootTemplateElement(String templateContents) {
    final Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(templateContents);
    return parserDiv;
  }

  public static Map<String, Element> getDataFieldElements(final Element parserDiv) {
    Element templateRoot = parserDiv.getFirstChildElement();
    final Map<String, Element> childTemplateElements = new HashMap<String, Element>();

    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll
    Visit.accept(templateRoot, new Visitor() {
      @Override
      public void visit(VisitContext context, Element element) {
        if (element.hasAttribute("data-field")) {
          childTemplateElements.put(element.getAttribute("data-field"), element);
        }
      }
    });

    return childTemplateElements;
  }

  public static void initWidget(Composite component, Element rootTemplateElement) {
    initWidgetNative(component, new TemplateWidget(rootTemplateElement));
  }

  private static native void initWidgetNative(Composite component, Widget root) /*-{
		component.@com.google.gwt.user.client.ui.Composite::initWidget(Lcom/google/gwt/user/client/ui/Widget;)(root);
  }-*/;
}
