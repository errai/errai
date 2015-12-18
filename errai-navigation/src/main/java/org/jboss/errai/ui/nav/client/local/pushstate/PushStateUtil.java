package org.jboss.errai.ui.nav.client.local.pushstate;

/**
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class PushStateUtil {
  
  /**
   * @return True if Errai Pushstate has not been disabled and 
   * the browser supports PushState.
   */
  public static boolean isPushStateActivated() {
    return (isPushStateEnabled() && isPushStateSupported());
  }

  private static native boolean isPushStateSupported() /*-{
    if ($wnd.history.pushState) 
      return true;
    else
      return false;
  }-*/;
  
  private static native boolean isPushStateEnabled() /*-{
    if ($wnd.erraiPushStateEnabled)
       return true;
    else
       return false;
  }-*/;
  
  public static native void enablePushState(boolean enabled) /*-{
    $wnd.erraiPushStateEnabled = enabled;
  }-*/;
}
