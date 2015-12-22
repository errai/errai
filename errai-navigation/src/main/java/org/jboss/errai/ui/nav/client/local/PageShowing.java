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
 * Indicates that the target method should be called when the {@link Page}
 * it is a member of is about to be displayed in the navigation content
 * panel: after the page's {@link PageState} fields have been updated and
 * before it is displayed in the navigation content panel.
 * <p>
 * When the client-side application is bootstrapping (the page is loading in the
 * browser), the Navigation system waits until all Errai modules are fully
 * initialized before displaying the initial page. Hence, it is safe to make RPC
 * requests and to fire portable CDI events from within a {@link PageShowing}
 * method.
 * <p>
 * The target method is permitted an optional parameter of type
 * {@link HistoryToken}. If the parameter is present, the framework will pass in
 * the history token that caused the page to show. This is useful in cases where
 * not all history token key names are known at compile time, so
 * {@code @PageState} fields can't be declared to accept their values.
 * <p>
 * The target method's return type must be {@code void}.
 * <p>
 * The target method can have any access type: public, protected, default, or
 * private.
 * <p>
 * If the target method throws an exception when called, behaviour is undefined.
 *
 * @see Page
 * @see PageState
 * @see Navigation
 * @see PageShown
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageShowing {

}
