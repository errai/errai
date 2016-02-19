/**
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind;

import static org.jboss.errai.codegen.Parameter.finalOf;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.HasAnnotations;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.common.client.api.annotations.Element;
import org.jboss.errai.common.client.api.annotations.Properties;
import org.jboss.errai.common.client.api.annotations.Property;
import org.jboss.errai.common.client.ui.HasValue;
import org.jboss.errai.common.client.ui.NativeHasValueAccessors;
import org.jboss.errai.common.client.ui.NativeHasValueAccessors.Accessor;
import org.jboss.errai.ioc.client.api.IOCExtension;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.AbstractBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.FactoryBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCExtensionConfigurator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.CustomFactoryInjectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraph;
import org.jboss.errai.ioc.rebind.ioc.graph.api.DependencyGraphBuilder.InjectableType;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.graph.api.InjectionSite;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Qualifier;
import org.jboss.errai.ioc.rebind.ioc.graph.api.QualifierFactory;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.DefaultCustomFactoryInjectable;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.FactoryNameGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.impl.InjectableHandle;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ExtensionTypeCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableProvider;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.jboss.errai.ui.shared.TemplateUtil;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.TagName;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

/**
 * Satisfies injection points for DOM elements.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@IOCExtension
public class ElementProviderExtension implements IOCExtensionConfigurator {

  @Override
  public void configure(final IOCProcessingContext context, final InjectionContext injectionContext) {
  }

  @Override
  public void afterInitialization(final IOCProcessingContext context, final InjectionContext injectionContext) {
    final MetaClass gwtElement = MetaClassFactory.get(com.google.gwt.dom.client.Element.class);
    injectionContext.registerExtensionTypeCallback(new ExtensionTypeCallback() {

      @Override
      public void callback(final MetaClass type) {
        final Element elementAnno;
        final JsType jsTypeAnno;

        if (type.isAssignableTo(gwtElement)) {
          final TagName gwtTagNameAnno;
          if ((gwtTagNameAnno = type.getAnnotation(TagName.class)) != null) {
            processGwtUserElement(injectionContext, type, gwtTagNameAnno);
          }
        }
        else if ((elementAnno = type.getAnnotation(Element.class)) != null) {
          if ((jsTypeAnno = type.getAnnotation(JsType.class)) == null || !jsTypeAnno.isNative()) {
            throw new RuntimeException(
                    Element.class.getSimpleName() + " is only valid on native " + JsType.class.getSimpleName() + "s.");
          }

          processJsTypeElement(injectionContext, type, elementAnno);
        }
      }
    });
  }

  private static void processJsTypeElement(final InjectionContext injectionContext, final MetaClass type, final Element elementAnno) {
    registerInjectableProvider(injectionContext, type, elementAnno.value());
  }

  private static void processGwtUserElement(final InjectionContext injectionContext, final MetaClass type,
          final TagName anno) {
    registerInjectableProvider(injectionContext, type, anno.value());
  }

  private static void registerInjectableProvider(final InjectionContext injectionContext, final MetaClass type, final String... tagNames) {
    final Set<Property> properties = getProperties(type);
    for (final String tagName : tagNames) {
      final Qualifier qualifier = getNamedQualifier(injectionContext.getQualifierFactory(), tagName);
      final InjectableHandle handle = new InjectableHandle(type, qualifier);
      injectionContext.registerExactTypeInjectableProvider(handle, new InjectableProvider() {

        CustomFactoryInjectable injectable;

        @Override
        public CustomFactoryInjectable getInjectable(final InjectionSite injectionSite,
                final FactoryNameGenerator nameGenerator) {
          if (injectable == null) {
            final String factoryName = nameGenerator.generateFor(handle.getType(), handle.getQualifier(),
                    InjectableType.ExtensionProvided);
            final FactoryBodyGenerator generator = new AbstractBodyGenerator() {

              @Override
              protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
                      final Injectable injectable, final DependencyGraph graph, final InjectionContext injectionContext) {
                final List<Statement> stmts = new ArrayList<Statement>();
                final String elementVar = "element";

                stmts.add(declareFinalVariable(elementVar, com.google.gwt.dom.client.Element.class,
                        invokeStatic(Document.class, "get").invoke("createElement", loadLiteral(tagName))));

                for (final Property property : properties) {
                  stmts.add(loadVariable(elementVar).invoke("setPropertyString", loadLiteral(property.name()),
                          loadLiteral(property.value())));
                }

                final String retValVar = "retVal";
                stmts.add(declareFinalVariable(retValVar, type, invokeStatic(TemplateUtil.class, "nativeCast", loadVariable(elementVar))));
                if (typeHasValueWithOverlayMethods(type)) {
                  stmts.add(invokeStatic(NativeHasValueAccessors.class, "registerAccessor", loadVariable(retValVar), createAccessorImpl(type, retValVar)));
                }
                stmts.add(loadVariable(retValVar).returnValue());

                return stmts;
              }

            };
            injectable = new DefaultCustomFactoryInjectable(handle.getType(), handle.getQualifier(), factoryName,
                    Dependent.class, Collections.singletonList(WiringElementType.DependentBean), generator);
          }

          return injectable;
        }
      });
    }
  }

  private static boolean typeHasValueWithOverlayMethods(final MetaClass type) {
    final MetaMethod getValue;
    return type.isAssignableTo(HasValue.class)
            && ((getValue = type.getMethod("getValue", new Class[0])).isAnnotationPresent(JsOverlay.class)
                    || type.getMethod("setValue", getValue.getReturnType()).isAnnotationPresent(JsOverlay.class));
  }

  private static Object createAccessorImpl(final MetaClass type, final String varName) {
    final MetaClass propertyType = type.getMethod("getValue", new Class[0]).getReturnType();

    return ObjectBuilder.newInstanceOf(Accessor.class)
      .extend()
      .publicMethod(Object.class, "get")
      .append(loadVariable(varName).invoke("getValue").returnValue())
      .finish()
      .publicMethod(void.class, "set", finalOf(Object.class, "value"))
      .append(loadVariable(varName).invoke("setValue", castTo(propertyType, loadVariable("value"))))
      .finish()
      .finish();
  }

  private static Set<Property> getProperties(final MetaClass type) {
    final Set<Property> properties = new HashSet<Property>();

    final Property declaredProperty = type.getAnnotation(Property.class);
    final Properties declaredProperties = type.getAnnotation(Properties.class);

    if (declaredProperty != null) {
      properties.add(declaredProperty);
    }

    if (declaredProperties != null) {
      properties.addAll(Arrays.asList(declaredProperties.value()));
    }

    return properties;
  }

  private static Qualifier getNamedQualifier(final QualifierFactory factory, final String tagName) {
    return factory.forSource(new HasAnnotations() {

      private final Named named = new Named() {

        @Override
        public Class<? extends Annotation> annotationType() {
          return Named.class;
        }

        @Override
        public String value() {
          return tagName;
        }
      };

      @Override
      public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
        return Named.class.equals(annotation);
      }

      @Override
      public Annotation[] getAnnotations() {
        return new Annotation[] { named };
      }

      @SuppressWarnings("unchecked")
      @Override
      public <A extends Annotation> A getAnnotation(final Class<A> annotation) {
        if (isAnnotationPresent(annotation)) {
          return (A) named;
        }
        else {
          return null;
        }
      }
    });
  }

}
