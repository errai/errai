package org.jboss.errai.ui.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Errai UI Runtime Utility for handling {@link Template} composition.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class TemplateUtil {
  private TemplateUtil() {
  }

  /**
   * Replace the {@link Element} with thte data-field of the given
   * {@link String} with the root {@link Element} of the given {@link UIObject}
   */
  public static void compositeComponentReplace(String componentType, String templateFile, Widget field,
          final Map<String, Element> dataFieldElements, String fieldName) {
    if (field == null) {
      throw new IllegalStateException("Widget to be composited into [" + componentType + "] data-field [" + fieldName
              + "] was null. Did you forget to @Inject or initialize this @DataField?");
    }
    Element element = dataFieldElements.get(fieldName);
    if (element == null) {
      throw new IllegalStateException("Template [" + templateFile
              + "] did not contain data-field attribute for field [" + fieldName + "]");
    }
    System.out.println("Compositing @Replace [data-field=" + fieldName + "] element [" + element + "] with Component "
            + field.getClass().getName() + " [" + field.getElement() + "]");
    Element parentElement = element.getParentElement();

    try {
      final JsArray<Node> templateAttributes = getAttributes(element);
      for (int i = 0; i < templateAttributes.length(); i++) {
        final Node node = templateAttributes.get(i);
        field.getElement().setAttribute(node.getNodeName(), node.getNodeValue());
      }
      parentElement.replaceChild(field.getElement(), element);
      if (field instanceof HasHTML) {
        NodeList<Node> children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          field.getElement().appendChild(children.getItem(i));
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not replace Element with [data-field=" + fieldName
              + "] - Did you already @Insert or @Replace a parent Element?", e);
    }
  }

  public static void initWidget(Composite component, Element wrapped, Collection<Widget> dataFields) {
    initWidgetNative(component, new TemplateWidget(wrapped, dataFields));
    DOM.setEventListener(component.getElement(), component);
  }

  private static native void initWidgetNative(Composite component, Widget wrapped) /*-{
		component.@com.google.gwt.user.client.ui.Composite::initWidget(Lcom/google/gwt/user/client/ui/Widget;)(wrapped);
  }-*/;

  public static Element getRootTemplateElement(String templateContents, final String rootField) {
    Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(templateContents);

    if (rootField != null && !rootField.trim().isEmpty()) {
      System.out.println("Locating root element: " + rootField);
      VisitContext<Element> context = Visit.accept(parserDiv, new Visitor<Element>() {
        @Override
        public void visit(VisitContextMutable<Element> context, Element element) {
          if (element.hasAttribute("data-field") && element.getAttribute("data-field").equals(rootField)) {
            Element result = DOM.createDiv();
            result.appendChild(element);
            context.setResult(result);
            context.setVisitComplete();
          }
        }
      });

      if (context.getResult() != null) {
        parserDiv = context.getResult();
      }
      else {
        throw new IllegalStateException("Could not locate Element in template with data-field=[" + rootField + "]\n"
                + parserDiv.getInnerHTML());
      }
    }

    System.out.println(parserDiv.getInnerHTML().trim());

    return parserDiv.getFirstChildElement();
  }

  public static Map<String, Element> getDataFieldElements(final Element templateRoot) {
    final Map<String, Element> childTemplateElements = new HashMap<String, Element>();

    System.out.println("Searching template for fields.");
    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll
    Visit.accept(templateRoot, new Visitor<Object>() {
      @Override
      public void visit(VisitContextMutable<Object> context, Element element) {
        if (element.hasAttribute("data-field")) {
          System.out.println("Located field: " + element.getAttribute("data-field"));
          childTemplateElements.put(element.getAttribute("data-field"), element);
        }
      }
    });

    return childTemplateElements;
  }

  /**
   * Join strings inserting separator between them.
   */
  private static String join(String[] strings, String separator) {
    StringBuffer result = new StringBuffer();

    for (String s : strings) {
      if (result.length() != 0) {
        result.append(separator);
      }
      result.append(s);
    }

    return result.toString();
  }

  private static native JsArray<Node> getAttributes(Element elem) /*-{
		return elem.attributes;
  }-*/;

}
