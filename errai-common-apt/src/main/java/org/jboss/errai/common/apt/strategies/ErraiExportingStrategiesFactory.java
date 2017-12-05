/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.common.apt.strategies;

import org.jboss.errai.codegen.meta.MetaClass;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static org.jboss.errai.codegen.meta.MetaClassFactory.loadClass;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiExportingStrategiesFactory {

  private final Elements elements;

  public ErraiExportingStrategiesFactory(final Elements elements) {
    this.elements = elements;
  }

  public ExportingStrategies buildFrom(final Class<?>... classes) {
    return new ExportingStrategies(stream(classes).flatMap(c -> stream(c.getDeclaredMethods()))
            .filter(m -> m.isAnnotationPresent(ErraiExportingStrategy.class))
            .filter(m -> Modifier.isStatic(m.getModifiers()))
            .collect(toMap(this::getStrategyAnnotationTypeElement, ReflectionExportingStrategy::new)));
  }

  private TypeElement getStrategyAnnotationTypeElement(final Method method) {
    return elements.getTypeElement(method.getAnnotation(ErraiExportingStrategy.class).value());
  }

  private static class ReflectionExportingStrategy implements ExportingStrategy {

    private final Method method;

    private ReflectionExportingStrategy(final Method method) {
      this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<ExportedElement> getExportedElements(final Element element) {
      try {
        // Methods have to be static
        return (Stream<ExportedElement>) method.invoke(null, element);
      } catch (final Exception e) {
        final String className = method.getDeclaringClass().getName();
        throw new RuntimeException("Error executing exporting strategy method " + method.getName() + " in " + className, e);
      }
    }
  }
}
