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

package org.jboss.errai.common.apt.module;

import org.jboss.errai.common.apt.configuration.TestAnnotation;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@TestAnnotation
public class AnnotatedTypeWithAnnotatedInnerClasses {

  @TestAnnotation
  public static class InnerAnnotatedStaticType {
  }

  @TestAnnotation
  public class InnerAnnotatedType {
  }

  @TestAnnotation
  // Should not be exported
  protected static class InnerNonVisibleType1 {
  }

  @TestAnnotation
  // Should not be exported
  static class InnerNonVisibleType2 {
  }

  @TestAnnotation
  // Should not be exported
  private static class InnerNonVisibleType3 {
  }

  @TestAnnotation
  // Should not be exported
  private class InnerNonVisibleType4 {
  }

  @TestAnnotation
  // Should not be exported
  protected class InnerNonVisibleType5 {
  }

  @TestAnnotation
          // Should not be exported
  class InnerNonVisibleType6 {
  }

}
