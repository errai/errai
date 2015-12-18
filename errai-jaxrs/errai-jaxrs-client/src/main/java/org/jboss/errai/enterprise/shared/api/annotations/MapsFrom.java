/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.ext.ExceptionMapper;

/**
 * <p>
 * An annotation that provides a mechanism for the developer to narrow down the scope
 * of a client-side {@link ExceptionMapper} to just a subset of REST Interfaces being
 * used by the application.  This annotation allows the developer to apply different
 * {@link ExceptionMapper} implementations to different REST APIs, in case the APIs
 * being used by the application do not share a common set of errors.
 * </p>
 * 
 * @author eric.wittmann@redhat.com
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MapsFrom {
  
  Class<?> [] value();

}
