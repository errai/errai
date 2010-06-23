/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.ioc.client.api.Bootstrapper;

import java.util.Map;

public class Container implements EntryPoint {
    public void onModuleLoad() {
        final Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);
               
        final RootPanel rootPanel = RootPanel.get();
        final InterfaceInjectionContext ctx = bootstrapper.bootstrapContainer();

        for (Widget w : ctx.getToRootPanel()) {
            rootPanel.add(w);
        }

        for (Map.Entry<Widget, String> entry : ctx.getWidgetToPanel().entrySet()) {
            ctx.getPanels().get(entry.getValue()).add(entry.getKey());
        }
    }
}
