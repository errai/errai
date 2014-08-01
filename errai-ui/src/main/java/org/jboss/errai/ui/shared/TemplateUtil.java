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
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.jboss.errai.ui.shared.wrapper.ElementWrapper;

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
 * @author Max Barkley <mbarkley@redhat.com>
 */
public final class TemplateUtil {
  private static final Logger logger = Logger.getLogger(TemplateUtil.class.getName());
  private static TranslationService translationService = null;

  public static TranslationService getTranslationService() {
    if (translationService == null) {
      translationService = GWT.create(TranslationService.class);
    }
    return translationService;
  }

  private TemplateUtil() {
  }

  /**
   * Replace the {@link Element} with the data-field of the given
   * {@link String} with the root {@link Element} of the given {@link UIObject}
   */
  public static void compositeComponentReplace(String componentType, String templateFile, Widget field,
          final Map<String, Element> dataFieldElements, String fieldName) {
    if (field == null) {
      throw new IllegalStateException("Widget to be composited into [" + componentType + "] field [" + fieldName
              + "] was null. Did you forget to @Inject or initialize this @DataField?");
    }
    Element element = dataFieldElements.get(fieldName);
    if (element == null) {
      throw new IllegalStateException("Template [" + templateFile
              + "] did not contain data-field, id or class attribute for field [" + componentType + "." + fieldName + "]");
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

      boolean hasI18nKey = !field.getElement().getAttribute("data-i18n-key").equals("");
      boolean hasI18nPrefix = !field.getElement().getAttribute("data-i18n-prefix").equals("");
      
      /*
       * Preserve template Element attributes.
       */
      final JsArray<Node> templateAttributes = getAttributes(element);
      for (int i = 0; i < templateAttributes.length(); i++) {
        final Node node = templateAttributes.get(i);
        String name = node.getNodeName();
        String oldValue = node.getNodeValue();
        /*
         * If this new component is templated, do not overwrite i18n related attributes.
         */
        if ((name.equals("data-i18n-key") || name.equals("data-role") && oldValue.equals("dummy"))
                && (hasI18nKey || hasI18nPrefix))
          continue;
        field.getElement().setAttribute(name, oldValue);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not replace Element with [data-field=" + fieldName + "]" +
      		" - Did you already @Insert or @Replace a parent Element?" +
      		" Is an element referenced by more than one @DataField?", e);
    }
  }

  public static void initWidget(Composite component, Element wrapped, Collection<Widget> dataFields) {
    if (!(component instanceof ListWidget)) {
      initWidgetNative(component, new TemplateWidget(wrapped, dataFields));
    }
    
    DOM.setEventListener(component.getElement(), component);
    StyleBindingsRegistry.get().updateStyles(component);
  }

  private static native void initWidgetNative(Composite component, Widget wrapped) /*-{
    component.@com.google.gwt.user.client.ui.Composite::initWidget(Lcom/google/gwt/user/client/ui/Widget;)(wrapped);
  }-*/;

  public static Element getRootTemplateElement(String templateContents, final String rootField) {
    Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(templateContents);

    if (rootField != null && !rootField.trim().isEmpty()) {
      logger.fine("Locating root element: " + rootField);
      VisitContext<TaggedElement> context = Visit.depthFirst(parserDiv, new Visitor<TaggedElement>() {
        @Override
        public boolean visit(VisitContextMutable<TaggedElement> context, Element element) {
          for (AttributeType attrType : AttributeType.values()) {
            String attrName = attrType.getAttributeName();
            TaggedElement existingCandidate = context.getResult();
            if (element.hasAttribute(attrName) && element.getAttribute(attrName).equals(rootField)
                && (existingCandidate == null || existingCandidate.getAttributeType().ordinal() < attrType.ordinal())) {
              context.setResult(new TaggedElement(attrType, element));
            }
          }
          return true;
        }
      });

      if (context.getResult() != null) {
        parserDiv = DOM.createDiv();
        parserDiv.appendChild(context.getResult().getElement());
      }
      else {
        throw new IllegalStateException("Could not locate Element in template with data-field, id or class = [" + rootField + "]\n"
                + parserDiv.getInnerHTML());
      }
    }

    logger.fine(parserDiv.getInnerHTML().trim());

    final Element templateRoot = firstNonMetaElement(parserDiv);
    if (templateRoot == null) {
      throw new IllegalStateException("Could not find template root for this template: " + templateContents);
    }
    else {
      return templateRoot;
    }
  }

  /*
   * This ignores meta tags from ERRAI-779.
   */
  private static Element firstNonMetaElement(final Element parserDiv) {
    Element displayable = parserDiv.getFirstChildElement();
    while (displayable != null && displayable.getTagName().equalsIgnoreCase("meta")) {
      displayable = displayable.getNextSiblingElement();
    }

    return displayable;
  }

  /**
   * Indicates the type of attribute a data field was discovered from.
   */
  private enum AttributeType {
    CLASS("class"),
    ID("id"),
    DATA_FIELD("data-field");
    
    private final String attributeName;
    
    AttributeType(String attributeName) {
      this.attributeName = attributeName;
    }
    
    public String getAttributeName() {
      return attributeName;
    }
  }
  
  private static class TaggedElement {
    private final AttributeType attributeType;
    private final Element element;
    
    public TaggedElement(AttributeType attributeType, Element element) {
      this.attributeType = attributeType;
      this.element = element;
    }
    
    public AttributeType getAttributeType() {
      return attributeType;
    }
    
    public Element getElement() {
      return element;
    }
  }
  
  /**
   * Called to perform i18n translation on the given template. Add i18n-prefix attribute to root of
   * template to allow translation after bean creation.
   * 
   * @param templateRoot
   */
  public static void translateTemplate(String templateFile, Element templateRoot) {
    if (!getTranslationService().isEnabled())
      return;

    logger.fine("Translating template: " + templateFile);
    final String i18nKeyPrefix = getI18nPrefix(templateFile);

    // Add i18n prefix attribute for post-creation translation
    templateRoot.setAttribute("data-i18n-prefix", i18nKeyPrefix);

    DomVisit.visit(new ElementWrapper(templateRoot), new TemplateTranslationVisitor(i18nKeyPrefix));
  }

  /**
   * Generate an i18n key prefix from the given template filename.
   * 
   * @param templateFile
   */
  public static String getI18nPrefix(String templateFile) {
    int idx1 = templateFile.lastIndexOf('/');
    int idx2 = templateFile.lastIndexOf('.');
    return templateFile.substring(idx1 + 1, idx2 + 1);
  }

  public static Map<String, Element> getDataFieldElements(final Element templateRoot) {
    final Map<String, TaggedElement> childTemplateElements = new LinkedHashMap<String, TaggedElement>();

    logger.fine("Searching template for fields.");
    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll
    Visit.depthFirst(templateRoot, new Visitor<Object>() {
      @Override
      public boolean visit(VisitContextMutable<Object> context, Element element) {
        for (AttributeType attrType : AttributeType.values()) {
          String attrName = attrType.getAttributeName();
          if (element.hasAttribute(attrName)) {
            logger.fine("Located " + attrName + ": " + element.getAttribute(attrName));
            for (String dataFieldName : element.getAttribute(attrName).split(" +")) {
              TaggedElement existingCandidate = childTemplateElements.get(dataFieldName);
              if (existingCandidate == null || existingCandidate.getAttributeType().ordinal() < attrType.ordinal()) {
                childTemplateElements.put(dataFieldName, new TaggedElement(attrType, element));
              }
            }
          }
        }
        return true;
      }
    });

    Map<String, Element> untaggedTemplateElements = new LinkedHashMap<String, Element>();
    for (Map.Entry<String, TaggedElement> entry : childTemplateElements.entrySet()) {
      untaggedTemplateElements.put(entry.getKey(), entry.getValue().getElement());
    }

    return untaggedTemplateElements;
  }

  @SuppressWarnings("deprecation")
  public static void setupNativeEventListener(Composite component, Element element, EventListener listener,
          int eventsToSink) {

    if (element == null) {
      throw new RuntimeException("A native event source was specified in " + component.getClass().getName()
              + " but the corresponding data-field does not exist!");
    }
    // These casts must stay to maintain compatibility with GWT 2.5.1
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
