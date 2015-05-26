package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.ui.nav.client.local.pushstate.HistoryImplPushState;
import org.jboss.errai.ui.nav.client.local.pushstate.PushStateUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;

/**
 * Dispatches to either {@link HistoryImplPushState} or GWT's default
 * {@link History}. At runtime, if HTML5 pushstate is supported, the former
 * implementation is used.
 *
 * <br> Also see <a href="https://github.com/jbarop/gwt-pushstate">GWT PushState</a>
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com
 */
public class HistoryWrapper {

  // See https://github.com/jbarop/gwt-pushstate/pull/14
  public static String pathPrefix = "/";

  public static interface HistorySupport {
      String tokenDispayName(String historyToken);
  }

  public static HistorySupport historySupport;

  private static HistoryImplPushState pushStateHistory;

  private static String initialDocTitle;
  private static String initialToken;

  private static HandlerRegistration internalValueChangeReg;
  private static final ValueChangeHandler<String> internalValueChangeHandler =
      new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          checkInitialDocTitle();
          setDocTitle(getDocTitleByToken(event.getValue()));
        }
      };

  private HistoryWrapper() {
  }

  static {
    if (!PushStateUtil.isPushStateActivated()) {
      internalValueChangeReg = History.addValueChangeHandler(internalValueChangeHandler);
    } else {
      // will be added later in maybeInitPushState() to allow safe pathPrefix assignment
    }
  }

  /**
   * @see History#addValueChangeHandler(ValueChangeHandler)
   */
  public static HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
    final HandlerRegistration reg;
    if (PushStateUtil.isPushStateActivated()) {
      maybeInitPushState();
      reg = pushStateHistory.addValueChangeHandler(handler);
    }
    else {
      reg = History.addValueChangeHandler(handler);
    }
    return reg;
  }

  /**
   * @see History#newItem(String, boolean)
   */
  public static void newItem(String historyToken, boolean fireEvent) {
    checkInitialDocTitle();
    final String s = getDocTitleByToken(historyToken);
    setDocTitle(s);
    if (PushStateUtil.isPushStateActivated()) {
      maybeInitPushState();
      pushStateHistory.newItem(historyToken, s, fireEvent);
    }
    else {
      History.newItem(historyToken, fireEvent);
    }
  }

  public static void replaceItem(String historyToken, boolean fireEvent) {
    checkInitialDocTitle();
    final String s = getDocTitleByToken(historyToken);
    setDocTitle(s);
    if (PushStateUtil.isPushStateActivated()) {
      maybeInitPushState();
      pushStateHistory.replaceItem(historyToken, s, fireEvent);
    }
    else {
      History.replaceItem(historyToken, fireEvent);
    }
  }

  private static void checkInitialDocTitle() {
    if (initialDocTitle == null) {
      initialDocTitle = getDocTitle();
      initialToken = getCurrentToken();
    }
  }

  private static String getDocTitleByToken(String historyToken) {
    String s;
    if (historyToken == null || historyToken.isEmpty() || initialToken.equals(historyToken)) {
        s = initialDocTitle;
    } else {
        s = historySupport != null ? historySupport.tokenDispayName(historyToken) : historyToken;
        s = s != null && !s.isEmpty() ? initialDocTitle + " - " + s : initialDocTitle;
    }
    return s;
  }

  private static native String getDocTitle() /*-{
    return $doc.title;
  }-*/;

  private static native void setDocTitle(String title) /*-{
    // the same idea as in History.js
    try {
      $doc.getElementsByTagName('title')[0].innerHTML = title.replace('<','&lt;').replace('>','&gt;').replace(' & ',' &amp; ');
    }
    catch ( Exception ) { }
    $doc.title = title;
  }-*/;

  public static String getCurrentToken() {
      if (PushStateUtil.isPushStateActivated()) {
          maybeInitPushState();
          return pushStateHistory.getCurrentToken();
      } else {
          return History.getToken();
      }
  }

  public static void back() {
      History.back();
  }

  public static void forward() {
      History.forward();
  }

  /**
   * @param value - back() is -1, forward() is 1
   **/
  public static native void go(int value) /*-{
      $wnd.history.go(value);
  }-*/;

  public static native int getLength() /*-{
      var len = $wnd.history.length;
      return len;
  }-*/;

  /**
   * @see History#fireCurrentHistoryState()
   */
  public static void fireCurrentHistoryState() {
    if (PushStateUtil.isPushStateActivated()) {
      maybeInitPushState();
      pushStateHistory.fireCurrentHistoryState();
    }
    else {
      History.fireCurrentHistoryState();
    }
  }

  private static void maybeInitPushState() {
    if (pushStateHistory == null) {
      pushStateHistory = new HistoryImplPushState();
      pushStateHistory.init(pathPrefix);
      internalValueChangeReg = pushStateHistory.addValueChangeHandler(internalValueChangeHandler);
    }
  }
}