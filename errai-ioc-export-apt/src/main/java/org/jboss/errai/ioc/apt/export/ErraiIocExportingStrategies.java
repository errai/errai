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

package org.jboss.errai.ioc.apt.export;

import org.jboss.errai.codegen.meta.impl.apt.APTClassUtil;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.IOC_PRODUCER;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.PRODUCES;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiIocExportingStrategies {

  @ErraiExportingStrategy(PRODUCES)
  public static Stream<ExportedElement> producesStrategy(final Element element) {
    if (element.getKind().isInterface() || element.getKind().isClass()) {
      return Stream.of(new ExportedElement(getTypeElement(PRODUCES), element));
    }
    return Stream.of(new ExportedElement(getTypeElement(PRODUCES), element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(IOC_PRODUCER)
  public static Stream<ExportedElement> iocProducerStrategy(final Element element) {
    return Stream.of(new ExportedElement(getTypeElement(IOC_PRODUCER), element.getEnclosingElement()));
  }

  private static TypeElement getTypeElement(final String annotationFqcn) {
    return APTClassUtil.elements.getTypeElement(annotationFqcn);
  }
}