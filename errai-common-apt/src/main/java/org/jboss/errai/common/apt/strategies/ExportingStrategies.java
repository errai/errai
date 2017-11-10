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

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportingStrategies {

  private final Map<TypeElement, ExportingStrategy> strategies;

  public static ExportingStrategies defaultStrategies() {
    return new ExportingStrategies(emptyMap());
  }

  ExportingStrategies(final Map<TypeElement, ExportingStrategy> strategies) {
    this.strategies = strategies;
  }

  public Stream<ExportedElement> getExportedElements(final TypeElement annotation, final Element element) {
    return strategies.getOrDefault(annotation, e -> getDefaultStrategy(annotation, e)).getExportedElements(element);
  }

  private Stream<ExportedElement> getDefaultStrategy(final TypeElement annotation, final Element element) {
    return ExportingStrategy.defaultGetElements(annotation, element);
  }
}
