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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getTypeElement;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.ALTERNATIVE;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.APPLICATION_SCOPED;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.CODE_DECORATOR;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.DEPENDENT;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.ENTRY_POINT;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.GOOGLE_INJECT;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.IOC_BOOTSTRAP_TASK;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.IOC_EXTENSION;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.IOC_PRODUCER;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.IOC_PROVIDER;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.JAVAX_INJECT;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.JS_TYPE;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.PRODUCES;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.QUALIFIER;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.SCOPE_CONTEXT;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.SHARED_SINGLETON;
import static org.jboss.errai.ioc.apt.export.SupportedAnnotationTypes.SINGLETON;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public interface ErraiIocExportingStrategies {

  @ErraiExportingStrategy(PRODUCES)
  static Stream<ExportedElement> produces(final Element element) {
    final TypeElement annotation = getTypeElement(PRODUCES);

    // Type
    if (element.getKind().isInterface() || element.getKind().isClass()) {
      return Stream.of(new ExportedElement(annotation, element));
    }

    // Method
    return Stream.of(new ExportedElement(annotation, element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(IOC_PRODUCER)
  static Stream<ExportedElement> iocProducer(final Element element) {
    return Stream.of(new ExportedElement(getTypeElement(IOC_PRODUCER), element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(JAVAX_INJECT)
  static Stream<ExportedElement> javaxInject(final Element element) {
    return anyInject(element, getTypeElement(JAVAX_INJECT));
  }

  @ErraiExportingStrategy(GOOGLE_INJECT)
  static Stream<ExportedElement> googleInject(final Element element) {
    return anyInject(element, getTypeElement(GOOGLE_INJECT));
  }

  @ErraiExportingStrategy(IOC_BOOTSTRAP_TASK)
  void iocBootstrapTask();

  @ErraiExportingStrategy(IOC_EXTENSION)
  void iocExtension();

  @ErraiExportingStrategy(CODE_DECORATOR)
  void codeDecorator();

  @ErraiExportingStrategy(SCOPE_CONTEXT)
  void scopeContext();

  @ErraiExportingStrategy(IOC_PROVIDER)
  void iocProvider();

  @ErraiExportingStrategy(DEPENDENT)
  void dependent();

  @ErraiExportingStrategy(APPLICATION_SCOPED)
  void applicationScoped();

  @ErraiExportingStrategy(ALTERNATIVE)
  void alternative();

  @ErraiExportingStrategy(SINGLETON)
  void singleton();

  @ErraiExportingStrategy(ENTRY_POINT)
  void entryPoint();

  @ErraiExportingStrategy(SHARED_SINGLETON)
  void sharedSingleton();

  @ErraiExportingStrategy(QUALIFIER)
  void qualifier();

  @ErraiExportingStrategy(JS_TYPE)
  void jsType();

  static Stream<ExportedElement> anyInject(final Element element, final TypeElement annotation) {
    final Stream<ExportedElement> type = Stream.of(new ExportedElement(annotation, element.getEnclosingElement()));

    if (element.getKind().isField()) {
      return concat(type, Stream.of(new ExportedElement(annotation, APTClassUtil.types.asElement(element.asType()))));
    }

    final Stream<ExportedElement> parameters = ((ExecutableElement) element).getParameters()
            .stream()
            .map(p -> new ExportedElement(annotation, p));

    return concat(type, parameters);
  }
}