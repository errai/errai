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

import org.jboss.errai.ui.nav.client.local.api.NavigationControl;

import java.lang.annotation.*;

/**
 * Indicates that the target method should be called when the {@link Page} it belongs to is
 * about to be authorized, pre {@link PageShowing}.
 * <p>
 * The target method is permitted an optional parameter of type {@link NavigationControl}. If the
 * parameter is present, the page navigation will not be carried out until
 * {@link NavigationControl#proceed()} is invoked. This is useful for interrupting page navigations
 * and then resuming at a later time (for example, to prompt the user to save their work before
 * transitioning to a new page).
 * <p>
 * Page loading can be interrupted by calling {@link NavigationControl#interrupt()}.
 * This allows for page redirection rather than proceeding a pages navigation.
 * <p>
 * The target method's return type will must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see Navigation
 * @author Ben Dol
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageAuthorize {

}
