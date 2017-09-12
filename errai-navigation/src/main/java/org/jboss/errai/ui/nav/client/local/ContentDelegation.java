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

package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.ui.nav.client.local.api.DelegationControl;

/**
 * Content delegation control interface.
 *
 * @author Ben Dol
 */
public interface ContentDelegation {

    /**
     * Called when the page is showing its content (setting the container widget).
     *
     * @param page the current page being shown.
     * @param defaultContainer the default content container.
     * @param widget the widget reference object for the page.
     * @param previousPage the previous page, <b>this can be null</b>.
     * @param control the delegation control for proceeding navigation process.
     */
    void showContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object previousPage,
                     DelegationControl control);

    /**
     * Called when the page is hiding its content (clearing container).
     *
     * @param page the current page being hidden.
     * @param defaultContainer the default content container.
     * @param widget the widget reference object for the page.
     * @param nextPage potential next requested page, <b>this can be null</b>.
     * @param control the delegation control for proceeding navigation process.
     */
    void hideContent(Object page, NavigatingContainer defaultContainer, IsWidget widget, Object nextPage,
                     DelegationControl control);
}
