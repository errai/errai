/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.rebind.ioc.element;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TagName;
import jsinterop.annotations.JsType;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.AbstractBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Satisfies injection points for DOM elements.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Tiago Bento <tfernand@redhat.com>
 */
@IOCExtension
public class ElementProviderExtension implements IOCExtensionConfigurator {

  private static final MetaClass ELEMENTAL_ELEMENT_META_CLASS = MetaClassFactory.get(elemental2.dom.Element.class);

  private static final MetaClass GWT_ELEMENT_META_CLASS = MetaClassFactory.get(com.google.gwt.dom.client.Element.class);

  @Override
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext) {
  }

  @Override
  public void afterInitialization(final IOCProcessingContext context, final InjectionContext injectionContext) {
    injectionContext.registerExtensionTypeCallback(type -> {
      try {

        if (type.isAssignableTo(ELEMENTAL_ELEMENT_META_CLASS)) {
          processElemental2Element(injectionContext, type);
        } else if (type.isAssignableTo(GWT_ELEMENT_META_CLASS)) {
          processGwtUserElement(injectionContext, type);
        } else {
          processJsTypeElement(injectionContext, type);
        }

      } catch (final Throwable t) {
        final String typeName = type.getFullyQualifiedName();
        final String className = ElementProviderExtension.class.getSimpleName();
        final String msg = String.format("Error occurred while processing [%s] in %s.", typeName, className);
        throw new RuntimeException(msg, t);
      }
    });
  }

  private static void processElemental2Element(final InjectionContext injectionContext, final MetaClass type) {
    elemental2ElementTags(type).stream()
            .map(tag -> exactTypeInjectableProvider(injectionContext, type, tag))
            .forEach(e -> registerExactTypeInjectableProvider(injectionContext, e));
  }

  static Collection<String> elemental2ElementTags(final MetaClass type) {
    final Collection<String> customElementTags = getCustomElementTags(type);

    if (!customElementTags.isEmpty()) {
      return customElementTags;
    }

    return Elemental2TagMapping.getTags(type.unsafeAsClass());
  }

  private static ExactTypeInjectableProvider exactTypeInjectableProvider(final InjectionContext injectionContext,
          final MetaClass type, final String tagName) {

    final Qualifier qualifier = injectionContext.getQualifierFactory().forSource(new HasNamedAnnotation(tagName));
    final InjectableHandle handle = new InjectableHandle(type, qualifier);

    final AbstractBodyGenerator injectionBodyGenerator = new ElementInjectionBodyGenerator(type, tagName);
    final ElementProvider elementProvider = new ElementProvider(handle, injectionBodyGenerator);

    return new ExactTypeInjectableProvider(handle, elementProvider);
  }

  private static void registerExactTypeInjectableProvider(final InjectionContext injectionContext,
          final ExactTypeInjectableProvider exactTypeInjectableProvider) {

    final InjectableHandle handle = exactTypeInjectableProvider.handle;
    final ElementProvider elementProvider = exactTypeInjectableProvider.elementProvider;

    injectionContext.registerExactTypeInjectableProvider(handle, elementProvider);
  }

  private static Collection<String> getCustomElementTags(final MetaClass type) {

    final Element elementAnnotation = type.unsafeGetAnnotation(Element.class);
    if (elementAnnotation == null) {
      return Collections.emptyList();
    }

    final JsType jsTypeAnnotation = type.unsafeGetAnnotation(JsType.class);
    if (jsTypeAnnotation == null || !jsTypeAnnotation.isNative()) {
      final String element = Element.class.getSimpleName();
      final String jsType = JsType.class.getSimpleName();
      throw new RuntimeException(element + " is only valid on native " + jsType + "s.");
    }

    return Arrays.asList(elementAnnotation.value());
  }

  private static class ExactTypeInjectableProvider {

    private final InjectableHandle handle;
    private final ElementProvider elementProvider;

    private ExactTypeInjectableProvider(InjectableHandle handle, ElementProvider elementProvider) {
      this.handle = handle;
      this.elementProvider = elementProvider;
    }
  }

  private static void processJsTypeElement(final InjectionContext injectionContext, final MetaClass type) {
    getCustomElementTags(type).stream()
            .map(tagName -> gwtExactTypeInjectableProvider(injectionContext, type, tagName))
            .forEach(e -> registerExactTypeInjectableProvider(injectionContext, e));
  }

  private static void processGwtUserElement(final InjectionContext injectionContext, final MetaClass type) {
    final TagName gwtTagNameAnnotation = type.unsafeGetAnnotation(TagName.class);
    if (gwtTagNameAnnotation != null) {
      Arrays.stream(gwtTagNameAnnotation.value())
              .map(tagName -> gwtExactTypeInjectableProvider(injectionContext, type, tagName))
              .forEach(e -> registerExactTypeInjectableProvider(injectionContext, e));
    }
  }

  private static ExactTypeInjectableProvider gwtExactTypeInjectableProvider(final InjectionContext injectionContext,
          final MetaClass type, final String tagName) {

    final Qualifier qualifier = injectionContext.getQualifierFactory().forSource(new HasNamedAnnotation(tagName));
    final InjectableHandle handle = new InjectableHandle(type, qualifier);

    final ElementProvider elementProvider = new ElementProvider(handle,
            new ElementInjectionBodyGenerator(type, tagName, getProperties(type)) {

              @Override
              protected ContextualStatementBuilder elementInitialization() {
                return Stmt.invokeStatic(Document.class, "get").invoke("createElement", Stmt.loadLiteral(tagName));
              }

              @Override
              protected Class<?> elementClass() {
                return com.google.gwt.dom.client.Element.class;
              }

            });

    return new ExactTypeInjectableProvider(handle, elementProvider);
  }

  private static Set<Property> getProperties(final MetaClass type) {
    final Set<Property> properties = new HashSet<>();

    final Property declaredProperty = type.unsafeGetAnnotation(Property.class);
    final Properties declaredProperties = type.unsafeGetAnnotation(Properties.class);

    if (declaredProperty != null) {
      properties.add(declaredProperty);
    }

    if (declaredProperties != null) {
      properties.addAll(Arrays.asList(declaredProperties.value()));
    }

    return properties;
  }
}
