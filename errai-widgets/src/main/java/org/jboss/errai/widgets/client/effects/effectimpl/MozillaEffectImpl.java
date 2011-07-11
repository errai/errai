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

package org.jboss.errai.widgets.client.effects.effectimpl;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.widgets.client.effects.Effect;


public class MozillaEffectImpl implements Effect {
  public Timer doFade(final Element el, double duration, final int start, final int end) {
    Timer t = start < end ?
        new Timer() {
          int step = start;

          public void run() {
            step += 5;
            if (step < end) {
              _setOpacity(el, step);
            }
            else {
              _setOpacity(el, end);
              cancel();
            }
          }
        }
        :
        new Timer() {
          int step = end;

          public void run() {
            step -= 5;
            if (step > end) {
              _setOpacity(el, step);
            }
            else {
              _setOpacity(el, end);
              cancel();
            }
          }
        };

    t.scheduleRepeating(1);

    return t;
  }

  public void setOpacity(Element el, int opacity) {
    _setOpacity(el, opacity);
  }

  public static void _setOpacity(Element el, float opacity) {
    el.getStyle().setProperty("opacity", String.valueOf(opacity / 100));
  }

}
