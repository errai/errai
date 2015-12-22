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

/**
 * Indicates that the target method should be called after the {@link Page}
 * it belongs to has been removed from the navigation content
 * panel.
 * <p>
 * The target method must not take any parameters.
 * <p>
 * The target method's return type must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see Navigation
 * @see PageHiding
 * @author Daniel Sachse <mail@w0mb.at>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageHidden {

}
