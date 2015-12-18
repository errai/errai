package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.ui.nav.client.local.pushstate.HistoryImplPushState;
import org.jboss.errai.ui.nav.client.local.pushstate.PushStateUtil;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.History;

/**
 * Dispatches to either {@link HistoryImplPushState} or GWT's default
 * {@link History}. At runtime, if HTML5 pushstate is supported, the former
 * implementation is used.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com
 */
public class HistoryWrapper {
  private static HistoryImplPushState pushStateHistory;

  private HistoryWrapper() {
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
    if (PushStateUtil.isPushStateActivated()) {
      maybeInitPushState();
      pushStateHistory.newItem(historyToken, fireEvent);      
    }
    else {
      History.newItem(historyToken, fireEvent);
    }
  }

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
      pushStateHistory.init();
    }
  }
}