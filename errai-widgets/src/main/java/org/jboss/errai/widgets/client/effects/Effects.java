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

package org.jboss.errai.widgets.client.effects;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;

public class Effects {
    /**
     * Load the browser-specific effect implementation.
     */
    private static Effect effect = GWT.create(Effect.class);
    static {
//        if (!GWT.isScript() && effect instanceof MozillaEffectImpl) {
//            effect = new Effect() {
//                public Timer doFade(Element el, double duration, int start, int end) {
//                    setOpacity(el, end);
//                    return null;
//                }
//
//                public void setOpacity(Element el, int opacity) {
//                    MozillaEffectImpl.setOpacityNative(el.getStyle(), opacity);
//                }
//            };
//        }
    }

    public static void fade(Element el, final double duration, final int start, final int end) {
        effect.doFade(el, duration, start, end);
    }

    public static void setOpacity(Element el, int opacity) {
        effect.setOpacity(el, opacity);
    }
}                                                                                             
