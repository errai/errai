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

import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportedElement;

import javax.lang.model.element.Element;
import java.util.stream.Stream;

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
  static Stream<ExportedElement> producesStrategy(final Element element) {
    if (element.getKind().isInterface() || element.getKind().isClass()) {
      return Stream.of(new ExportedElement(getTypeElement(PRODUCES), element));
    }
    return Stream.of(new ExportedElement(getTypeElement(PRODUCES), element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(IOC_PRODUCER)
  static Stream<ExportedElement> iocProducerStrategy(final Element element) {
    return Stream.of(new ExportedElement(getTypeElement(IOC_PRODUCER), element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(IOC_BOOTSTRAP_TASK)
  void iocBootstrapTaskStrategy();

  @ErraiExportingStrategy(IOC_EXTENSION)
  void iocExtensionStrategy();

  @ErraiExportingStrategy(CODE_DECORATOR)
  void codeDecoratorStrategy();

  @ErraiExportingStrategy(SCOPE_CONTEXT)
  void scopeContextStrategy();

  @ErraiExportingStrategy(JAVAX_INJECT)
  void javaxInjectStrategy();

  @ErraiExportingStrategy(GOOGLE_INJECT)
  void googleInjectStrategy();

  @ErraiExportingStrategy(IOC_PROVIDER)
  void iocProviderStrategy();

  @ErraiExportingStrategy(DEPENDENT)
  void dependentStrategy();

  @ErraiExportingStrategy(APPLICATION_SCOPED)
  void applicationScopedStrategy();

  @ErraiExportingStrategy(ALTERNATIVE)
  void alternativeStrategy();

  @ErraiExportingStrategy(SINGLETON)
  void singletonStrategy();

  @ErraiExportingStrategy(ENTRY_POINT)
  void entryPointStrategy();

  @ErraiExportingStrategy(SHARED_SINGLETON)
  void sharedSingletonStrategy();

  @ErraiExportingStrategy(QUALIFIER)
  void qualifierStrategy();

  @ErraiExportingStrategy(JS_TYPE)
  void jsTypeStrategy();
}