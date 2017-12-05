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

package org.jboss.errai.common.apt.generator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.elements;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportedTypesFromSource {

  private final Multimap<String, Element> exportedTypes = HashMultimap.create();

  public void putAll(final TypeElement annotationName, final Set<? extends Element> elementsAnnotatedWith) {
    exportedTypes.putAll(annotationName.getQualifiedName().toString(), elementsAnnotatedWith);
  }

  public Set<TypeElement> exportableAnnotations() {
    return exportedTypes.keys().stream().map(elements::getTypeElement).collect(toSet());
  }

  public Set<Element> findAnnotatedSourceElements(final TypeElement typeElement) {
    return new HashSet<>(exportedTypes.get(typeElement.getQualifiedName().toString()));
  }
}
