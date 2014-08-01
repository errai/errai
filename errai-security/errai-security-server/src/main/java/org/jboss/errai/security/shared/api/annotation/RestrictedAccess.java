/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package org.jboss.errai.security.shared.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.style.StyleBinding;

/**
 * Indicates that the service or element can only be accessed / shown by logged-in users who belong
 * to all of the given security roles.
 * <p>
 * This annotation can appear anywhere {@link Service} or
 * {@link org.jboss.errai.ui.shared.api.annotations.DataField} can appear.
 *
 * @see Service
 * @see org.jboss.errai.ui.shared.api.annotations.DataField
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@StyleBinding
@InterceptorBinding
public @interface RestrictedAccess {

  /**
   * The set of simple roles that the calling user must belong to in order to access the target
   * resource.
   */
  @Nonbinding
  String[] roles() default {};

  /**
   * The types of providers used to generated provided roles required to access the target resource.
   */
  @Nonbinding
  Class<? extends RequiredRolesProvider>[] providers() default {};

  /**
   * This CSS class name is applied to {@link DataField} annotated elements when
   * a user lacks authorization.
   */
  public static final String CSS_CLASS_NAME = "errai-restricted-access-style";
}
