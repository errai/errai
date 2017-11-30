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

import org.jboss.errai.common.apt.generator.AbstractErraiModuleExportFileGenerator;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

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
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ IOC_BOOTSTRAP_TASK, IOC_EXTENSION, CODE_DECORATOR, SCOPE_CONTEXT, JAVAX_INJECT, GOOGLE_INJECT, IOC_PROVIDER, DEPENDENT, APPLICATION_SCOPED, ALTERNATIVE, SINGLETON, IOC_PRODUCER, SHARED_SINGLETON, ENTRY_POINT, QUALIFIER, JS_TYPE, PRODUCES })
public class ErraiIocExportFileGenerator extends AbstractErraiModuleExportFileGenerator {

  @Override
  protected String getCamelCaseErraiModuleName() {
    return "ioc";
  }

  @Override
  protected Class<?> getExportingStrategiesClass() {
    return ErraiIocExportingStrategies.class;
  }
}