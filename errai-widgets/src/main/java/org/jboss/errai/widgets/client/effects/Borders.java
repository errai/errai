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

import com.google.gwt.user.client.ui.UIObject;

public class Borders {
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 4;
    public static final int BOTTOM = 8;
    public static final int ALL = LEFT + TOP + RIGHT + BOTTOM;

    public static native void simpleBorder(UIObject obj, int radius, int edges) /*-{
      var elem = obj.@com.google.gwt.user.client.ui.UIObject::getElement()();

    $wnd.RUZEE.ShadedBorder.create({corner:radius,
        edges:@org.jboss.errai.widgets.client.effects.Borders::convertWhere(I)(edges)}).render(elem);
     }-*/;

    public static native void simpleBorder(UIObject obj, int radius) /*-{
      var elem = obj.@com.google.gwt.user.client.ui.UIObject::getElement()();
       $wnd.RUZEE.ShadedBorder.create({corner:radius}).render(elem);
     }-*/;

    public static native void shadowBorder(UIObject obj, int radius, int shadowWidth) /*-{
      var elem = obj.@com.google.gwt.user.client.ui.UIObject::getElement()();
       $wnd.RUZEE.ShadedBorder.create({corner:radius, shadow: shadowWidth }).render(elem);
     }-*/;

    public static native String convertWhere(int where) /*-{
      var result = "";
      if (where & @org.jboss.errai.widgets.client.effects.Borders::LEFT) {
        result += "l";
      }
      if (where & @org.jboss.errai.widgets.client.effects.Borders::TOP) {
        result += "t";
      }
      if (where & @org.jboss.errai.widgets.client.effects.Borders::RIGHT) {
        result += "r";
      }
      if (where & @org.jboss.errai.widgets.client.effects.Borders::BOTTOM) {
        result += "b";
      }
      return result;
    }-*/;

}
