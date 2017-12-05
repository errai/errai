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
import org.jboss.errai.common.apt.generator.AbstractExportFileGenerator;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportedElement;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getTypeElement;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.ALTERNATIVE;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.APPLICATION_SCOPED;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.CODE_DECORATOR;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.DEPENDENT;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.ENTRY_POINT;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.GOOGLE_INJECT;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.IOC_BOOTSTRAP_TASK;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.IOC_EXTENSION;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.IOC_PRODUCER;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.IOC_PROVIDER;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.JAVAX_INJECT;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.JS_TYPE;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.PRODUCES;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.QUALIFIER;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.SCOPE_CONTEXT;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.SHARED_SINGLETON;
import static org.jboss.errai.ioc.apt.export.ErraiIocExportFileGenerator.SupportedAnnotationTypes.SINGLETON;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ IOC_BOOTSTRAP_TASK,
                            IOC_EXTENSION,
                            CODE_DECORATOR,
                            SCOPE_CONTEXT,
                            JAVAX_INJECT,
                            GOOGLE_INJECT,
                            IOC_PROVIDER,
                            DEPENDENT,
                            APPLICATION_SCOPED,
                            ALTERNATIVE,
                            SINGLETON,
                            IOC_PRODUCER,
                            SHARED_SINGLETON,
                            ENTRY_POINT,
                            QUALIFIER,
                            JS_TYPE,
                            PRODUCES })
public class ErraiIocExportFileGenerator extends AbstractExportFileGenerator {

  @Override
  protected String getCamelCaseErraiModuleName() {
    return "ioc";
  }

  @Override
  protected Class<?> getExportingStrategiesClass() {
    return ErraiIocExportingStrategies.class;
  }

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

  interface SupportedAnnotationTypes {

    String IOC_BOOTSTRAP_TASK = "org.jboss.errai.ioc.client.api.IOCBootstrapTask";
    String IOC_EXTENSION = "org.jboss.errai.ioc.client.api.IOCExtension";
    String CODE_DECORATOR = "org.jboss.errai.ioc.client.api.CodeDecorator";
    String SCOPE_CONTEXT = "org.jboss.errai.ioc.client.api.ScopeContext";
    String JAVAX_INJECT = "javax.inject.Inject";
    String GOOGLE_INJECT = "com.google.inject.Inject";
    String IOC_PROVIDER = "org.jboss.errai.ioc.client.api.IOCProvider";
    String DEPENDENT = "javax.enterprise.context.Dependent";
    String APPLICATION_SCOPED = "javax.enterprise.context.ApplicationScoped";
    String ALTERNATIVE = "javax.enterprise.inject.Alternative";
    String SINGLETON = "javax.inject.Singleton";
    String IOC_PRODUCER = "org.jboss.errai.common.client.api.annotations.IOCProducer";
    String ENTRY_POINT = "org.jboss.errai.ioc.client.api.EntryPoint";
    String SHARED_SINGLETON = "org.jboss.errai.ioc.client.api.SharedSingleton";
    String QUALIFIER = "javax.inject.Qualifier";
    String JS_TYPE = "jsinterop.annotations.JsType";
    String PRODUCES = "javax.enterprise.inject.Produces";
  }
}