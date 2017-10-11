/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import jsinterop.base.Js;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.ContextualStatementBuilder;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.common.client.api.annotations.Property;
import org.jboss.errai.common.client.ui.HasValue;
import org.jboss.errai.common.client.ui.NativeHasValueAccessors;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.AbstractBodyGenerator;
import org.jboss.errai.ioc.rebind.ioc.graph.api.Injectable;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.jboss.errai.codegen.Parameter.finalOf;
import static org.jboss.errai.codegen.util.Stmt.castTo;
import static org.jboss.errai.codegen.util.Stmt.declareFinalVariable;
import static org.jboss.errai.codegen.util.Stmt.invokeStatic;
import static org.jboss.errai.codegen.util.Stmt.loadLiteral;
import static org.jboss.errai.codegen.util.Stmt.loadStatic;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
class ElementInjectionBodyGenerator extends AbstractBodyGenerator {

  private final MetaClass type;
  private final String tagName;
  private final Set<Property> properties;

  ElementInjectionBodyGenerator(final MetaClass type, String tagName) {
    this(type, tagName, Collections.emptySet());
  }

  ElementInjectionBodyGenerator(final MetaClass type, String tagName, final Set<Property> properties) {
    this.type = type;
    this.tagName = tagName;
    this.properties = properties;
  }

  @Override
  protected List<Statement> generateCreateInstanceStatements(final ClassStructureBuilder<?> bodyBlockBuilder,
          final Injectable injectable, final InjectionContext injectionContext) {

    final List<Statement> stmts = new ArrayList<>();
    final String elementVar = "element";

    stmts.add(declareFinalVariable(elementVar, elementClass(), elementInitialization()));

    for (final Property property : properties) {
      stmts.add(loadVariable(elementVar).invoke("setPropertyString", loadLiteral(property.name()),
              loadLiteral(property.value())));
    }

    final String retValVar = "retVal";

    stmts.add(declareFinalVariable(retValVar, type, invokeStatic(Js.class, "cast", loadVariable(elementVar))));

    if (implementsNativeHasValueAndRequiresGeneratedInvocation(type)) {
      stmts.add(Stmt.invokeStatic(NativeHasValueAccessors.class, "registerAccessor", loadVariable(retValVar),
              createAccessorImpl(type, retValVar)));
    }

    stmts.add(loadVariable(retValVar).returnValue());
    return stmts;
  }

  protected ContextualStatementBuilder elementInitialization() {
    return loadStatic(DomGlobal.class, "document").invoke("createElement", tagName);
  }

  protected Class<?> elementClass() {
    return Element.class;
  }

  /**
   * If a type uses @JsOverlay or @JsProperty on overrides of HasValue methods, then we must generate
   * an invocation so the GWT compiler uses the correct JS invocation at runtime.
   */
  private static boolean implementsNativeHasValueAndRequiresGeneratedInvocation(final MetaClass type) {
    if (type.isAssignableTo(HasValue.class)) {
      final MetaClass hasValue = MetaClassFactory.get(HasValue.class);
      final MetaMethod getValue = type.getMethod("getValue", new MetaClass[0]);
      final MetaMethod setValue = type.getMethod("setValue", getValue.getReturnType());

      if (type.isInterface() && (getValue.getDeclaringClass().getErased().equals(hasValue)
              || setValue.getDeclaringClass().getErased().equals(hasValue))) {
        /*
         * In this case, the methods could be default implementations on an interface (not returned by TypeOracle) so we
         * will assume we need to generate an invocation.
         */
        return true;
      } else {
        final Stream<MetaAnnotation> getAnnos = getValue.getAnnotations().stream();
        final Stream<MetaAnnotation> setAnnos = setValue.getAnnotations().stream();

        final Predicate<MetaAnnotation> testForOverlayOrProperty = anno -> anno.annotationType()
                .getPackageName()
                .equals("jsinterop.annotations");

        return getAnnos.anyMatch(testForOverlayOrProperty) || setAnnos.anyMatch(testForOverlayOrProperty);
      }
    }

    return false;
  }

  private static Object createAccessorImpl(final MetaClass type, final String varName) {
    final MetaClass propertyType = type.getMethod("getValue", new Class[0]).getReturnType();

    return ObjectBuilder.newInstanceOf(NativeHasValueAccessors.Accessor.class)
            .extend()
            .publicMethod(Object.class, "get")
            .append(loadVariable(varName).invoke("getValue").returnValue())
            .finish()
            .publicMethod(void.class, "set", finalOf(Object.class, "value"))
            .append(loadVariable(varName).invoke("setValue", castTo(propertyType, loadVariable("value"))))
            .finish()
            .finish();
  }
}
