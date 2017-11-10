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

import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@FunctionalInterface
public interface ExportingStrategy {

  Stream<ExportedElement> getExportedElements(final Element annotatedElement);

  static Stream<ExportedElement> defaultGetElements(final TypeElement annotation, final Element element) {
    if (element.getKind().isClass() || element.getKind().isInterface()) {
      return Stream.of(new ExportedElement(annotation, element));
    } else if (element.getKind().isField()) {
      return Stream.of(new ExportedElement(annotation, APTClassUtil.types.asElement(element.asType())));
    } else if (element.getKind().equals(METHOD) || element.getKind().equals(CONSTRUCTOR)) {
      return ((ExecutableElement) element).getParameters().stream().map(p -> new ExportedElement(annotation, p));
    } else if (element.getKind().equals(PARAMETER)) {
      return Stream.of(new ExportedElement(annotation, element));
    } else {
      return Stream.of();
    }
  }

  static ExportingStrategy defaultStrategy(final TypeElement annotation) {
    return e -> ExportingStrategy.defaultGetElements(annotation, e);
  }
}
