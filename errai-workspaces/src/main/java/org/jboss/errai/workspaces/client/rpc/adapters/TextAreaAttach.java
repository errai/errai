/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.workspaces.client.rpc.adapters;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.workspaces.client.rpc.Attachable;

public class TextAreaAttach implements Attachable {
    public void attach(final String id, final Widget widget, final String packet) {
        final TextArea w = (TextArea) widget;

//        w.addChangeListener(
//                new ChangeListener() {
//                    public void onChange(Widget widget) {
//                        packet.setParameter(id, w.getText());
//                        packet.notifySessionState();
//                    }
//                }
//        );
//
//        String val = packet.getParameter(id);
//        if (val != null) {
//            w.setText(val);
//        }
    }
}
