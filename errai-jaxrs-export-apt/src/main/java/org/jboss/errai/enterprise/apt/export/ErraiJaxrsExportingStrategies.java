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

import org.jboss.errai.common.apt.strategies.ErraiExportingStrategy;
import org.jboss.errai.common.apt.strategies.ExportedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.stream.Stream;

import static org.jboss.errai.codegen.meta.impl.apt.APTClassUtil.getTypeElement;
import static org.jboss.errai.enterprise.apt.export.SupportedAnnotationTypes.FEATURE_INTERCEPTOR;
import static org.jboss.errai.enterprise.apt.export.SupportedAnnotationTypes.INTERCEPTED_CALL;
import static org.jboss.errai.enterprise.apt.export.SupportedAnnotationTypes.INTERCEPTS_REMOTE_CALL;
import static org.jboss.errai.enterprise.apt.export.SupportedAnnotationTypes.PATH;
import static org.jboss.errai.enterprise.apt.export.SupportedAnnotationTypes.PROVIDER;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public interface ErraiJaxrsExportingStrategies {

  @ErraiExportingStrategy(PATH)
  static Stream<ExportedElement> path(final Element element) {
    final TypeElement pathAnnotation = getTypeElement(PATH);
    if (element.getKind().isInterface() || element.getKind().isClass()) {
      return Stream.of(new ExportedElement(pathAnnotation, element));
    }
    return Stream.of(new ExportedElement(pathAnnotation, element.getEnclosingElement()));
  }

  @ErraiExportingStrategy(PROVIDER)
  void provider();

  @ErraiExportingStrategy(INTERCEPTED_CALL)
  void interceptedCall();

  @ErraiExportingStrategy(FEATURE_INTERCEPTOR)
  void featureInterceptor();

  @ErraiExportingStrategy(INTERCEPTS_REMOTE_CALL)
  void interceptsRemoteCall();

}
