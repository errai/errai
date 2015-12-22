/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Declares that the target type &#8212;
 * <em>which must be a subtype of Widget, implement {@link IsWidget}, or be a {@link Templated} component &#8212;</em>
 * is a named page with optional state information within the Errai Navigation system.
 *
 * @see TransitionTo
 * @see PageState
 * @see PageRole
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Page {

  /**
   * The path component that identifies this page. If not specified, the page's
   * name will be the simple name of the class.
   */
  String path() default "";

  /**
   * Defines the roles of the page. You can group pages together by defining roles
   * that extend either {@link PageRole} or {@link UniquePageRole} a example of this is
   * the {@link DefaultPage} indicating that this page is the starting page.
   *
   * @return the roles that this page belongs to
   */
  Class<? extends PageRole>[] role() default {};
}
