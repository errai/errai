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

import org.jboss.errai.common.apt.generator.AbstractExportFileGenerator;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.CLIENT_MARSHALLER;
import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.CUSTOM_MAPPING;
import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.ENVIRONMENT_CONFIG_EXTENSION;
import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.NON_PORTABLE;
import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.PORTABLE;
import static org.jboss.errai.marshalling.apt.export.ErraiMarshallingExportFileGenerator.SupportedAnnotationTypes.SERVER_MARSHALLER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ PORTABLE,
                            NON_PORTABLE,
                            CLIENT_MARSHALLER,
                            SERVER_MARSHALLER,
                            ENVIRONMENT_CONFIG_EXTENSION,
                            CUSTOM_MAPPING })
public class ErraiMarshallingExportFileGenerator extends AbstractExportFileGenerator {

  @Override
  protected String getCamelCaseErraiModuleName() {
    return "marshalling";
  }

  interface SupportedAnnotationTypes {

    String PORTABLE = "org.jboss.errai.common.client.api.annotations.Portable";
    String NON_PORTABLE = "org.jboss.errai.common.client.api.annotations.NonPortable";
    String CLIENT_MARSHALLER = "org.jboss.errai.marshalling.client.api.annotations.ClientMarshaller";
    String SERVER_MARSHALLER = "org.jboss.errai.marshalling.client.api.annotations.ServerMarshaller";
    String CUSTOM_MAPPING = "org.jboss.errai.marshalling.rebind.api.CustomMapping";

    String ENVIRONMENT_CONFIG_EXTENSION = "org.jboss.errai.config.rebind.EnvironmentConfigExtension";
  }
}