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

import com.google.gwt.dom.client.TagName;
import jsinterop.annotations.JsType;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ui.rebind.ioc.element.elemental.Elemental2InjectionBodyGenerator;
import org.jboss.errai.ui.rebind.ioc.element.elemental.Elemental2Mapping;
import org.jboss.errai.ui.rebind.ioc.element.gwt.GwtElementInjectionBodyGenerator;

import java.util.Optional;
import java.util.function.Function;

/**
 * Satisfies injection points for DOM elements.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@IOCExtension
public class ElementProviderExtension implements IOCExtensionConfigurator {

  private static final MetaClass GWT_ELEMENT_META_CLASS = MetaClassFactory.get(com.google.gwt.dom.client.Element.class);
  private static final MetaClass ELEMENTAL_ELEMENT_META_CLASS = MetaClassFactory.get(elemental2.dom.Element.class);

  @Override
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext) {
  }

  @Override
  public void afterInitialization(final IOCProcessingContext context, final InjectionContext injectionContext) {
    injectionContext.registerExtensionTypeCallback(type -> extensionCallback(injectionContext, type));
  }

  private void extensionCallback(final InjectionContext injectionContext, final MetaClass type) {
    try {
      final Element elementAnno;

      if (type.isAssignableTo(ELEMENTAL_ELEMENT_META_CLASS)) {
        processElementalElement(injectionContext, type);
      } else if (type.isAssignableTo(GWT_ELEMENT_META_CLASS)) {
        processGwtUserElement(injectionContext, type);
      } else if ((elementAnno = type.getAnnotation(Element.class)) != null) {
        final JsType jsTypeAnno;
        if ((jsTypeAnno = type.getAnnotation(JsType.class)) == null || !jsTypeAnno.isNative()) {
          throw new RuntimeException(
                  Element.class.getSimpleName() + " is only valid on native " + JsType.class.getSimpleName() + "s.");
        }

        processJsTypeElement(injectionContext, type, elementAnno);
      }
    } catch (final Throwable t) {
      final String typeName = type.getFullyQualifiedName();
      final String className = ElementProviderExtension.class.getSimpleName();
      throw new RuntimeException(String.format("Error occurred while processing [%s] in %s.", typeName, className), t);
    }
  }

  private static void processElementalElement(final InjectionContext injectionContext, final MetaClass type) {
    Optional<String[]> mappedTags = Elemental2Mapping.tagsFor(type.asClass());
    mappedTags.ifPresent(tags -> registerInjectableProvider(injectionContext, type,
            tagName -> new Elemental2InjectionBodyGenerator(tagName, type), tags));

    //FIXME: tiago: leave it noop?
  }

  private static void processJsTypeElement(final InjectionContext injectionContext, final MetaClass type,
          final Element elementAnnotation) {
    registerInjectableProvider(injectionContext, type, tagName -> new GwtElementInjectionBodyGenerator(tagName, type),
            elementAnnotation.value());
  }

  private static void processGwtUserElement(final InjectionContext injectionContext, final MetaClass type) {
    final TagName gwtTagNameAnnotation = type.getAnnotation(TagName.class);
    if (gwtTagNameAnnotation != null) {
      registerInjectableProvider(injectionContext, type, tagName -> new GwtElementInjectionBodyGenerator(tagName, type),
              gwtTagNameAnnotation.value());
    }
  }

  private static void registerInjectableProvider(final InjectionContext injectionContext, final MetaClass type,
          final Function<String, ElementInjectionBodyGenerator> elementInjectionBodyGeneratorProducer,
          final String... tagNames) {

    for (final String tagName : tagNames) {
      final Qualifier qualifier = injectionContext.getQualifierFactory().forSource(new HasNamedAnnotation(tagName));
      final InjectableHandle handle = new InjectableHandle(type, qualifier);

      final ElementProvider elementProvider = new ElementProvider(handle,
              elementInjectionBodyGeneratorProducer.apply(tagName));

      injectionContext.registerExactTypeInjectableProvider(handle, elementProvider);
    }
  }

}
