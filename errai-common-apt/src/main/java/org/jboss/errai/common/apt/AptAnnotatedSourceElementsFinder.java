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

package org.jboss.errai.common.apt;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AptAnnotatedSourceElementsFinder implements AnnotatedSourceElementsFinder {

  private RoundEnvironment roundEnvironment;

  public AptAnnotatedSourceElementsFinder(final RoundEnvironment roundEnvironment) {
    this.roundEnvironment = roundEnvironment;
  }

  @Override
  public Set<? extends Element> findSourceElementsAnnotatedWith(final TypeElement typeElement) {
    return roundEnvironment.getElementsAnnotatedWith(typeElement);
  }

  @Override
  public Set<? extends Element> findSourceElementsAnnotatedWith(final Class<? extends Annotation> annotationClass) {
    return roundEnvironment.getElementsAnnotatedWith(annotationClass);
  }
}
