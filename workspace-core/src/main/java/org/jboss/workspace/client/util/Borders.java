package org.jboss.workspace.client.util;

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
        edges:@org.jboss.workspace.client.util.Borders::convertWhere(I)(edges)}).render(elem);
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
      if (where & @org.jboss.workspace.client.util.Borders::LEFT) {
        result += "l";
      }
      if (where & @org.jboss.workspace.client.util.Borders::TOP) {
        result += "t";
      }
      if (where & @org.jboss.workspace.client.util.Borders::RIGHT) {
        result += "r";
      }
      if (where & @org.jboss.workspace.client.util.Borders::BOTTOM) {
        result += "b";
      }
      return result;
    }-*/;

}
