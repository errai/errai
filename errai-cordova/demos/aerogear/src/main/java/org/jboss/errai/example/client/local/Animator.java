package org.jboss.errai.example.client.local;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.dom.client.Element;
import org.jboss.errai.example.client.local.util.DefaultCallback;

/**
 * @author edewit@redhat.com
 */
public abstract class Animator extends Composite {

  public static native void show(Element element) /*-{
      var target = $wnd.$(element);
      target.slideUp('slow');
      target.next().slideDown('slow');
  }-*/;

  public static native void hide(Element element, AsyncCallback<Void> callback) /*-{
      var target = $wnd.$(element);
      target.slideUp('slow', function() {
          callback.@com.google.gwt.user.client.rpc.AsyncCallback::onSuccess(Ljava/lang/Object;)(null);
      });
      target.prev().slideDown('slow');
  }-*/;

  public static void hide(Element element) {
    hide(element, new DefaultCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        //ignore
      }
    });
  }
}
