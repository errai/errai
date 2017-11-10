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

package org.jboss.errai.marshalling.apt.export;

import org.jboss.errai.common.apt.generator.AbstractErraiModuleExportFileGenerator;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.CLIENT_MARSHALLER;
import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.CUSTOM_MAPPING;
import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.ENVIRONMENT_CONFIG_EXTENSION;
import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.NON_PORTABLE;
import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.PORTABLE;
import static org.jboss.errai.marshalling.apt.export.SupportedAnnotationTypes.SERVER_MARSHALLER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ PORTABLE, NON_PORTABLE, CLIENT_MARSHALLER, SERVER_MARSHALLER, ENVIRONMENT_CONFIG_EXTENSION, CUSTOM_MAPPING })
public class ErraiMarshallingExportFileGenerator extends AbstractErraiModuleExportFileGenerator {

  @Override
  protected String getCamelCaseErraiModuleName() {
    return "marshalling";
  }

  @Override
  protected Class<?> getExportingStrategiesClass() {
    return ErraiMarshallingExportingStrategies.class;
  }
}