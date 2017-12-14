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

package org.jboss.errai.enterprise.apt.export;

import org.jboss.errai.common.apt.generator.AbstractExportFileGenerator;
import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportedElement;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getTypeElement;
import static org.jboss.errai.enterprise.apt.export.ErraiJaxrsExportFileGenerator.SupportedAnnotationTypes.FEATURE_INTERCEPTOR;
import static org.jboss.errai.enterprise.apt.export.ErraiJaxrsExportFileGenerator.SupportedAnnotationTypes.INTERCEPTED_CALL;
import static org.jboss.errai.enterprise.apt.export.ErraiJaxrsExportFileGenerator.SupportedAnnotationTypes.INTERCEPTS_REMOTE_CALL;
import static org.jboss.errai.enterprise.apt.export.ErraiJaxrsExportFileGenerator.SupportedAnnotationTypes.PATH;
import static org.jboss.errai.enterprise.apt.export.ErraiJaxrsExportFileGenerator.SupportedAnnotationTypes.PROVIDER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({ PATH, INTERCEPTED_CALL, FEATURE_INTERCEPTOR, INTERCEPTS_REMOTE_CALL, PROVIDER })
public class ErraiJaxrsExportFileGenerator extends AbstractExportFileGenerator {

  @Override
  protected String getCamelCaseErraiModuleName() {
    return "jaxrs";
  }

  @Override
  protected Class<?> getExportingStrategiesClass() {
    return ErraiJaxrsExportingStrategies.class;
  }

  public interface ErraiJaxrsExportingStrategies {

    @ErraiExportingStrategy(PATH)
    static Stream<ExportedElement> path(final Element element) {
      final TypeElement pathAnnotation = getTypeElement(PATH);
      if (element.getKind().isInterface() || element.getKind().isClass()) {
        return Stream.of(new ExportedElement(pathAnnotation, element));
      }
      return Stream.of(new ExportedElement(pathAnnotation, element.getEnclosingElement()));
    }
  }

  interface SupportedAnnotationTypes {
    String PATH = "javax.ws.rs.Path";
    String FEATURE_INTERCEPTOR = "org.jboss.errai.common.client.api.interceptor.FeatureInterceptor";
    String INTERCEPTED_CALL = "org.jboss.errai.common.client.api.interceptor.InterceptedCall";
    String INTERCEPTS_REMOTE_CALL = "org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall";
    String PROVIDER = "javax.ws.rs.ext.Provider";
  }
}