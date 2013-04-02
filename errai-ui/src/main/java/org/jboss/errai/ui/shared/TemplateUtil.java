/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Errai UI Runtime Utility for handling {@link Template} composition.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public final class TemplateUtil {
  private static final Logger logger = Logger.getLogger(TemplateUtil.class.getName());
  public static TranslationService translationService = GWT.create(TranslationService.class);

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
              + "] did not contain data-field attribute for field [" + componentType + "." + fieldName + "]");
    }
    logger.fine("Compositing @Replace [data-field=" + fieldName + "] element [" + element + "] with Component "
            + field.getClass().getName() + " [" + field.getElement() + "]");

    if (!element.getTagName().equals(field.getElement().getTagName())) {
      logger.warning("WARNING: Replacing Element type [" + element.getTagName() + "] with type ["
              + field.getElement().getTagName() + "]");
    }
    Element parentElement = element.getParentElement();

    try {
      if (field instanceof HasText) {
        Node firstNode = element.getFirstChild();
        while (firstNode != null) {
          if (firstNode != element.getFirstChildElement())
            field.getElement().appendChild(element.getFirstChild());
          else {
            field.getElement().appendChild(element.getFirstChildElement());
          }
          firstNode = element.getFirstChild();
        }
      }
      parentElement.replaceChild(field.getElement(), element);

      /*
       * Preserve template Element attributes.
       */
      final JsArray<Node> templateAttributes = getAttributes(element);
      for (int i = 0; i < templateAttributes.length(); i++) {
        final Node node = templateAttributes.get(i);
        field.getElement().setAttribute(node.getNodeName(), node.getNodeValue());
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
      logger.fine("Locating root element: " + rootField);
      VisitContext<Element> context = Visit.depthFirst(parserDiv, new Visitor<Element>() {
        @Override
        public boolean visit(VisitContextMutable<Element> context, Element element) {
          if (element.hasAttribute("data-field") && element.getAttribute("data-field").equals(rootField)) {
            Element result = DOM.createDiv();
            result.appendChild(element);
            context.setResult(result);
            context.setVisitComplete();
            return false;
          }
          return true;
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

    logger.fine(parserDiv.getInnerHTML().trim());

    return parserDiv.getFirstChildElement();
  }

  /**
   * Called to perform i18n translation on the given template.
   * @param templateRoot
   */
  public static void translateTemplate(String templateFile, Element templateRoot) {
    logger.fine("Translating template: " + templateFile);
    final String i18nKeyPrefix = getI18nPrefix(templateFile);
    Visit.depthFirst(templateRoot, new Visitor<Object>() {
      @Override
      public boolean visit(VisitContextMutable<Object> context, Element element) {
        // Developers can mark entire sections of the template as "do not translate"
        if ("true".equals(element.getAttribute("data-i18n-skip"))) {
          return false;
        }
        // If the element either explicitly enables i18n (via an i18n key) or is a text-only
        // node, translate it.
        if (element.hasAttribute("data-i18n-key") || isTextOnly(element)) {
          translateElement(i18nKeyPrefix, element);
          return false;
        }

        if (element.hasAttribute("title")) {
          translateAttribute(i18nKeyPrefix, element, "title");
        }
        if (element.hasAttribute("placeholder")) {
          translateAttribute(i18nKeyPrefix, element, "placeholder");
        }
        return true;
      }

      /**
       * Translates the given element.
       * @param i18nKeyPrefix
       * @param element
       */
      private void translateElement(String i18nKeyPrefix, Element element) {
        String translationKey = i18nKeyPrefix + getTranslationKey(element);
        String translationValue = getI18nValue(translationKey);
        if (translationValue != null)
          element.setInnerHTML(translationValue);
      }

      /**
       * Translates an attribute of the given element.
       * @param i18nKeyPrefix
       * @param element
       * @param attributeName
       */
      private void translateAttribute(String i18nKeyPrefix, Element element, String attributeName) {
        String elementKey = null;
        if (element.hasAttribute("data-field")) {
          elementKey = element.getAttribute("data-field");
        } else if (element.hasAttribute("id")) {
          elementKey = element.getAttribute("id");
        } else if (element.hasAttribute("name")) {
          elementKey = element.getAttribute("name");
        } else {
          elementKey = getTranslationKey(element);
        }
        // If we couldn't figure out a key for this thing, then just bail.
        if (elementKey == null || elementKey.trim().length() == 0) {
          return;
        }
        String translationKey = i18nKeyPrefix + elementKey;
        translationKey += "-" + attributeName;
        String translationValue = getI18nValue(translationKey);
        if (translationValue != null)
          element.setAttribute(attributeName, translationValue);
      }

      /**
       * Gets a translation key associated with the given element.
       * @param element
       */
      protected String getTranslationKey(Element element) {
        String translationKey = null;
        String currentText = element.getInnerText();
        if (element.hasAttribute("data-i18n-key")) {
          translationKey = element.getAttribute("data-i18n-key");
        } else {
          translationKey = currentText.replaceAll("[:\\s'\"]", "_");
          if (translationKey.length() > 128) {
            translationKey = translationKey.substring(0, 128) + translationKey.hashCode();
          }
        }
        return translationKey;
      }

      /**
       * Returns true if the given element has some text and no element children.
       * @param element
       */
      private boolean isTextOnly(Element element) {
        String text = element.getInnerText();
        return (element.getFirstChildElement() == null) && text != null && text.trim().length() > 0;
      }

      /**
       * Looks up the proper value for the given translation key.  Uses the
       * TranslationService to lookup the proper value for the current locale.
       * @param translationKey
       */
      private String getI18nValue(String translationKey) {
        return translationService.getTranslation(translationKey);
      }

    });
  }

  /**
   * Generate an i18n key prefix from the given template filename.
   * @param templateFile
   */
  private static String getI18nPrefix(String templateFile) {
    int idx1 = templateFile.lastIndexOf('/');
    int idx2 = templateFile.lastIndexOf('.');
    return templateFile.substring(idx1 + 1, idx2 + 1);
  }

  public static Map<String, Element> getDataFieldElements(final Element templateRoot) {
    final Map<String, Element> childTemplateElements = new LinkedHashMap<String, Element>();

    logger.fine("Searching template for fields.");
    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll
    Visit.depthFirst(templateRoot, new Visitor<Object>() {
      @Override
      public boolean visit(VisitContextMutable<Object> context, Element element) {
        if (element.hasAttribute("data-field")) {
          logger.fine("Located field: " + element.getAttribute("data-field"));
          childTemplateElements.put(element.getAttribute("data-field"), element);
        }
        return true;
      }
    });

    return childTemplateElements;
  }

  public static void setupNativeEventListener(Composite component, Element element, EventListener listener,
          int eventsToSink) {

    if (element == null) {
      throw new RuntimeException("A native event source was specified in " + component.getClass().getName()
          + " but the corresponding data-field does not exist!");
    }
    DOM.setEventListener((com.google.gwt.user.client.Element) element, listener);
    DOM.sinkEvents((com.google.gwt.user.client.Element) element, eventsToSink);
  }

  public static <T extends EventHandler> Widget setupPlainElementEventHandler(Composite component, Element element,
          T handler, com.google.gwt.event.dom.client.DomEvent.Type<T> type) {
    ElementWrapperWidget widget = ElementWrapperWidget.getWidget(element);
    widget.addDomHandler(handler, type);
    // TODO add to Composite as child.
    return widget;
  }

  public static <T extends EventHandler> void setupWrappedElementEventHandler(Composite component, Widget widget,
          T handler, com.google.gwt.event.dom.client.DomEvent.Type<T> type) {
    widget.addDomHandler(handler, type);
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
