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

package org.jboss.errai.codegen.apt.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * An annotation processor that dynamically loads a test class and runs test methods provided by a
 * {@link TestControlService}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@SupportedAnnotationTypes("org.jboss.errai.codegen.apt.util.Hook")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class UnitTestRunningAnntoationProcessor extends AbstractProcessor {

  private final TestControlService service;

  public UnitTestRunningAnntoationProcessor(final TestControlService service) {
    this.service = service;
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    try {
      final String testClassName = service.getTestClassName();
      final Class<?> testClass = Class.forName(testClassName);
      final Object instance = testClass.newInstance();
      final Method setTypes = testClass.getMethod("setTypes", Types.class);
      final Method setElements = testClass.getMethod("setElements", Elements.class);
      setTypes.invoke(instance, processingEnv.getTypeUtils());
      setElements.invoke(instance, processingEnv.getElementUtils());

      String nextTest;
      while ((nextTest = service.requestNextTest()) != null) {
        final Method test = testClass.getMethod(nextTest);
        try {
          test.invoke(instance);
          service.submitSuccess();
        } catch (final InvocationTargetException e) {
          if (e.getCause() != null) {
            service.submitFailure(e.getCause());
          }
          else {
            throw e;
          }
        } catch (final Throwable t) {
          service.submitFailure(t);
        }
      }
    } catch (ClassNotFoundException | InstantiationException
            | IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }

    return false;
  }

}
