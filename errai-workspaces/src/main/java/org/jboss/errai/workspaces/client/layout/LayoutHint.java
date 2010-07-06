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

package org.jboss.errai.workspaces.client.layout;

import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.workspaces.client.protocols.LayoutParts;

import java.util.LinkedHashMap;

public class LayoutHint {
    private static LinkedHashMap<Widget, LayoutHintProvider> MANAGED_WIDGETS = new LinkedHashMap<Widget, LayoutHintProvider>();
    private static LinkedHashMap<String, LayoutHintProvider> MANAGED_SUBJECTS = new LinkedHashMap<String, LayoutHintProvider>();

    private static int counter = 0;

    public static void attach(final Widget w, LayoutHintProvider p) {
        String subject = "local:org.jboss.errai.sizeHints:" + counter++;

        ErraiBus.get().subscribe(subject,
                new MessageCallback() {
                    public void callback(Message message) {
                        w.setPixelSize(message.get(Double.class, LayoutParts.Width).intValue(),
                                message.get(Double.class, LayoutParts.Height).intValue());
                    }
                });


        MANAGED_WIDGETS.put(w, p);
        MANAGED_SUBJECTS.put(subject, p);
    }

    public static LayoutHintProvider findProvider(Widget instance) {
        return MANAGED_WIDGETS.get(instance);
    }

    public static LayoutHintProvider findProvider(String subject) {
        return MANAGED_SUBJECTS.get(subject);
    }

    public static void hintAll() {
        LayoutHintProvider p;
        for (String s : MANAGED_SUBJECTS.keySet()) {
            if ((p = findProvider(s)) != null && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                MessageBuilder.createMessage()
                        .toSubject(s)
                        .with(LayoutParts.Width, p.getWidthHint())
                        .with(LayoutParts.Height, p.getHeightHint())
                        .noErrorHandling().sendNowWith(ErraiBus.get());
            }
        }

        for (Widget w : MANAGED_WIDGETS.keySet()) {
            p = findProvider(w);
            if (p != null && w.isAttached() && p.getWidthHint() > 0 && p.getHeightHint() > 0) {
                w.setPixelSize(p.getWidthHint(), p.getHeightHint());
            }
        }
    }
}
