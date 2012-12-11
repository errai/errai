/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation allows users to indicate the default value for the i18n
 * replacement being assigned to the annotated element.  This annotation must
 * be used on an element that is <b>also</b> annotated with {@link I18n}.
 * </p>
 *
 * @author eric.wittmann@redhat.com
 */
@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface I18nDefault {

  /**
   * Specify the default value to use if the i18n key is not found in the bundle.  If this
   * is omitted and the key is not found in the bundle, then a {@link RuntimeException}
   * will be thrown.
   */
  String value();

}
