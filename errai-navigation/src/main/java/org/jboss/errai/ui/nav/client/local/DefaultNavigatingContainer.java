/*
 * Copyright 2013 JBoss, a division of Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.ui.nav.client.local;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Uses SimplePanel as a navigating container.
 *
 * @author Piotr Kosmowski
 */
public class DefaultNavigatingContainer implements NavigatingContainer {

    private final SimplePanel panel = new NavigationPanel();

    @Override
    public Widget getWidget() {
        return panel.getWidget();
    }

    @Override
    public void setWidget(IsWidget w) {
        panel.setWidget(w);
    }

    @Override
    public void setWidget(Widget w) {
        panel.setWidget(w);
    }

    @Override
    public void clear() {
        panel.clear();
    }

    @Override
    public Widget asWidget() {
        return panel.asWidget();
    }

}
