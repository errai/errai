/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

import jsinterop.annotations.JsType;

/**
 * A qualifier indicating that the injected type should be satisfied by a {@link JsType}, potentially defined in a
 * separately compiled script.
 *
 * This is useful when you have a locally defined type that implements a {@link JsType} interface. In this case adding
 * {@link External} to an injection site of the interface ensures that you will get an external implementation (if one
 * exists) rather than the local non-JsType one.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Documented
@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER })
public @interface External {

}
