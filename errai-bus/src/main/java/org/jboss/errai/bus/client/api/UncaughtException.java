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

package org.jboss.errai.bus.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jboss.errai.common.client.api.ErrorCallback;

/**
 * Indicates that the annotated method should be invoked when an exception occurs in the client-side
 * message bus that is not handled by an {@link ErrorCallback}.
 * 
 * The annotated method needs to have exactly one parameter of type {@link Throwable}. 
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ ElementType.METHOD })
public @interface UncaughtException {
}
