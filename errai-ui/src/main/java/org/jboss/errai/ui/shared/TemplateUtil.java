/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import org.jboss.errai.common.client.ui.ElementWrapperWidget;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ui.client.local.spi.TemplateProvider;
import org.jboss.errai.ui.client.local.spi.TemplateRenderingCallback;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.jboss.errai.ui.shared.wrapper.ElementWrapper;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import jsinterop.annotations.JsType;

/**
 * Errai UI Runtime Utility for handling {@link Template} composition.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
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
    logger.finer("Compositing @Replace [data-field=" + fieldName + "] element [" + element + "] with Component "
            + field.getClass().getName() + " [" + field.getElement() + "]");

    if (!element.getTagName().equals(field.getElement().getTagName())) {
      logger.warning("WARNING: Replacing Element type [" + element.getTagName() + "] with type ["
              + field.getElement().getTagName() + "]");
    }

    Element parentElement = element.getParentElement();
    try {
      if (field instanceof HasText && (!(field instanceof ElementWrapperWidget) || field.getElement().getChildCount() == 0)) {
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
        String value = node.getNodeValue();
        /*
         * If this new component is templated, do not overwrite i18n related attributes.
         */
        if ((name.equals("data-i18n-key") || name.equals("data-role") && value.equals("dummy"))
                && (hasI18nKey || hasI18nPrefix))
          continue;

        if (name.equals("class")) {
          // setAttribute for "class" does not work in IE8.
          field.getElement().setClassName(value);
        }
        else {
          field.getElement().setAttribute(name, value);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not replace Element with [data-field=" + fieldName + "]" +
            " - Did you already @Insert or @Replace a parent Element?" +
            " Is an element referenced by more than one @DataField?", e);
    }
  }

  public static Element asElement(Object element) {
    try {
      return nativeCast(element);
    } catch (Throwable t) {
      throw new RuntimeException("Error casting @DataField of type " + element.getClass().getName() + " to " + Element.class.getName(), t);
    }
  }

  /**
   * Only works for native {@link JsType JsTypes} and {@link JavaScriptObject JavaScriptObjects}.
   */
  public static native <T> T nativeCast(Object element) /*-{
    return element;
  }-*/;

  public static com.google.gwt.user.client.Element asDeprecatedElement(Object element) {
    try {
      return nativeCast(element);
    } catch (Throwable t) {
      throw new RuntimeException("Error casting @DataField of type " + element.getClass().getName() + " to "
              + com.google.gwt.user.client.Element.class.getName(), t);
    }
  }

  public static void initTemplated(Object templated, Element wrapped, Collection<Widget> dataFields) {
    final TemplateWidget widget = new TemplateWidget(wrapped, dataFields);
    TemplateWidgetMapper.put(templated, widget);
    StyleBindingsRegistry.get().updateStyles(templated);
    widget.onAttach();
    RootPanel.detachOnWindowClose(widget);
    TemplateInitializedEvent.fire(widget);
  }

  public static void cleanupTemplated(Object templated) {
    final TemplateWidget templateWidget = TemplateWidgetMapper.get(templated);
    TemplateWidgetMapper.remove(templated);
    if (RootPanel.isInDetachList(templateWidget)) {
      RootPanel.detachNow(templateWidget);
    }
  }

  public static void initWidget(Composite component, Element wrapped, Collection<Widget> dataFields) {
    if (!(component instanceof ListWidget)) {
      initWidgetNative(component, new TemplateWidget(wrapped, dataFields));
    }
    if (!component.isAttached()) {
      onAttachNative(component);
      RootPanel.detachOnWindowClose(component);
    }
    StyleBindingsRegistry.get().updateStyles(component);
    TemplateInitializedEvent.fire(component);
  }

  public static void cleanupWidget(Composite component) {
    if (RootPanel.isInDetachList(component)) {
      RootPanel.detachNow(component);
    }
  }

  private static native void onAttachNative(Widget w) /*-{
    w.@com.google.gwt.user.client.ui.Widget::onAttach()();
  }-*/;

  private static native void initWidgetNative(Composite component, Widget wrapped) /*-{
    component.@com.google.gwt.user.client.ui.Composite::initWidget(Lcom/google/gwt/user/client/ui/Widget;)(wrapped);
  }-*/;

  private static Map<String, Element> templateRoots = new HashMap<String, Element>();

  public static Element getRootTemplateParentElement(String templateContents, final String templateFileName, final String rootField) {
    String key = templateFileName + "#" + rootField;
    if (templateRoots.containsKey(key)) {
      return cloneIntoNewParent(templateRoots.get(key));
    }

    Element parserDiv = DOM.createDiv();
    parserDiv.setInnerHTML(templateContents);
    if (rootField != null && !rootField.trim().isEmpty()) {
      logger.finer("Locating root element: " + rootField);
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

    logger.finest(parserDiv.getInnerHTML().trim());

    final Element templateRoot = firstNonMetaElement(parserDiv);
    if (templateRoot == null) {
      throw new IllegalStateException("Could not find template root for this template: " + templateContents);
    }
    else {
      templateRoots.put(key, templateRoot);
      return cloneIntoNewParent(templateRoot);
    }
  }

  public static Element getRootTemplateElement(final Element rootParent) {
    return firstNonMetaElement(rootParent);
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

    logger.finer("Translating template: " + templateFile);
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

    final Map<String, Element> dataFields = new LinkedHashMap<String, Element>();
    final Map<String, TaggedElement> childTemplateElements = new LinkedHashMap<String, TaggedElement>();

    logger.finer("Searching template for fields.");
    // TODO do this as browser split deferred binding using
    // Document.querySelectorAll() -
    // https://developer.mozilla.org/En/DOM/Element.querySelectorAll
    Visit.depthFirst(templateRoot, new Visitor<Object>() {
      @Override
      public boolean visit(VisitContextMutable<Object> context, Element element) {
        for (AttributeType attrType : AttributeType.values()) {
          String attrName = attrType.getAttributeName();
          String attrVal = element.getAttribute(attrName);
          if (attrVal != null && !attrVal.isEmpty()) {
            String[] attributeValues = (attrType == AttributeType.CLASS) ? attrVal.split(" +") : new String[]{attrVal};
            for (String dataFieldName : attributeValues) {
              TaggedElement existingCandidate = childTemplateElements.get(dataFieldName);
              if (existingCandidate == null || existingCandidate.getAttributeType().ordinal() < attrType.ordinal()) {
                childTemplateElements.put(dataFieldName, new TaggedElement(attrType, element));
                dataFields.put(dataFieldName, element);
              }
            }
          }
        }
        return true;
      }
    });

    return dataFields;
  }

  public static void setupNativeEventListener(Object component, ElementWrapperWidget wrapper, EventListener listener,
          int eventsToSink) {

    if (wrapper == null) {
      throw new RuntimeException("A native event source was specified in " + component.getClass().getName()
              + " but the corresponding data-field does not exist!");
    }
    wrapper.setEventListener(eventsToSink, listener);
  }

  /**
   * Use this for elements that are not wrapped by any widgets (including the ElementWrapperWidget).
   */
  public static void setupNativeEventListener(Object component, Element element, EventListener listener,
          int eventsToSink) {

    if (element == null) {
      throw new RuntimeException("A native event source was specified in " + component.getClass().getName()
              + " but the corresponding data-field does not exist!");
    }
    DOM.setEventListener(element, listener);
    DOM.sinkEvents(element, eventsToSink);
  }

  public static <T extends EventHandler> Widget setupPlainElementEventHandler(Composite component, Element element,
          T handler, com.google.gwt.event.dom.client.DomEvent.Type<T> type) {
    ElementWrapperWidget widget = ElementWrapperWidget.getWidget(element);
    widget.addDomHandler(handler, type);
    // TODO add to Composite as child.
    return widget;
  }

  public static <T extends EventHandler> void setupWrappedElementEventHandler(Widget widget,
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

  private static Element cloneIntoNewParent(Element element) {
    Element parent = DOM.createDiv();
    Element clone = DOM.clone(element, true);
    parent.appendChild(clone);
    return parent;
  }

  private final static class TemplateRequest {
    final Class<?> templateProvider;
    final String location;
    final TemplateRenderingCallback renderingCallback;

    TemplateRequest(Class<?> templateProvider, String location, TemplateRenderingCallback renderingCallback) {
      this.templateProvider = templateProvider;
      this.location = location;
      this.renderingCallback = renderingCallback;
    }
  }

  private static Queue<TemplateRequest> requests = new LinkedList<TemplateRequest>();

  /**
   * Called by the generated IOC bootstrapper if a provider is specified on a
   * templated composite (see {@link Templated#provider()}). This method will
   * make sure that templates will be provided and rendered in invocation order
   * even if a given provider is asynchronous.
   *
   * @param templateProvider
   *          the template provider to use for supplying the template, must not
   *          be null.
   * @param location
   *          the location of the template, must not be null.
   * @param renderingCallback
   *          the callback to invoke when the template is available, must not be
   *          null.
   */
  public static void provideTemplate(final Class<?> templateProvider, final String location,
          final TemplateRenderingCallback renderingCallback) {

    TemplateRequest request = new TemplateRequest(templateProvider, location, renderingCallback);
    requests.add(request);
    if (requests.size() == 1) {
      provideNextTemplate();
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static void provideNextTemplate() {
    if (requests.isEmpty())
      return;

    try {
      final TemplateRequest request = requests.peek();
      IOC.getAsyncBeanManager().lookupBean(request.templateProvider).getInstance(new CreationalCallback() {
        @Override
        public void callback(Object bean) {
          TemplateProvider provider = ((TemplateProvider) bean);
          try {
            provider.provideTemplate(request.location, new TemplateRenderingCallback() {
              @Override
              public void renderTemplate(String template) {
                request.renderingCallback.renderTemplate(template);
                requests.remove();
                provideNextTemplate();
              }
            });
          }
          catch (RuntimeException t) {
            requests.remove();
            throw t;
          }
        }
      });
    }
    catch (IOCResolutionException ioce) {
      requests.remove();
      throw ioce;
    }
  }
}
