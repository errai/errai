/*
 * Copyright 2012 Johannes Barop
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jboss.errai.ui.nav.client.local.pushstate;

import java.util.logging.Logger;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

/**
 * Enhances GWT's History implementation to add HTML5 pushState support.
 * 
 * <p>
 * This class no longer inherits from HistoryImpl to allow for compatibility
 * with both GWT 2.6 and GWT 2.7+. HistoryImpl was moved in GWT 2.7 and is no
 * longer accessible. The previously inherited methods are now part of this
 * class.
 * </p>
 * 
 * <p>
 * The complete path is treated as history token.
 * </p>
 * 
 * <p>
 * The leading '/' is hidden from GWTs History API, so that the path '/' is
 * returned as an empty history token ('').
 * </p>
 * 
 * @author <a href="mailto:jb@barop.de">Johannes Barop</a>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class HistoryImplPushState implements HasValueChangeHandlers<String> {
  private static final Logger LOG = Logger.getLogger(HistoryImplPushState.class.getName());

  private HandlerManager handlers = new HandlerManager(null);
  private String token = "";
  
  public boolean init() {
    // initialize HistoryImpl with the current path
    updateHistoryToken(Window.Location.getPath() + Window.Location.getQueryString());
    // initialize the empty state with the current history token
    nativeUpdate(token, true);
    // initialize the popState handler
    initPopStateHandler();

    return true;
  }
  
  private void nativeUpdate(final String historyToken) {
    nativeUpdate(historyToken, false);
  }
  
  private void nativeUpdate(final String historyToken, boolean replace) {
    String newPushStateToken = CodeServerParameterHelper.append(encodeFragment(historyToken));
    if (!newPushStateToken.startsWith("/")) {
      newPushStateToken = "/" + newPushStateToken;
    }
    
    if (replace){
      replaceState(newPushStateToken);
      if (LogConfiguration.loggingIsEnabled()) {
        LOG.fine("Replaced '" + newPushStateToken + "' (" + historyToken + ")");
      }
    }else{
      pushState(newPushStateToken);
      if (LogConfiguration.loggingIsEnabled()) {
        LOG.fine("Pushed '" + newPushStateToken + "' (" + historyToken + ")");
      }
    }
  }

  /**
   * Set the current path as GWT History token which can later retrieved with
   * {@link History#getToken()}.
   */
  private void updateHistoryToken(String path) {
    String[] split = path.split("\\?");
    String token = split[0];
    token = (token.length() > 0) ? decodeFragment(token) : "";
    token = (token.startsWith("/")) ? token.substring(1) : token;

    String queryString = (split.length == 2) ? split[1] : "";
    queryString = CodeServerParameterHelper.remove(queryString);
    if (queryString != null && !queryString.trim().isEmpty()) {
      token += "?" + queryString;
    }

    if (LogConfiguration.loggingIsEnabled()) {
      LOG.fine("Set token to '" + token + "'");
    }
    this.token = token;
  }

  /**
   * Initialize an event handler that gets executed when the token changes.
   */
  private native void initPopStateHandler() /*-{
    var that = this;
    var oldHandler = $wnd.onpopstate;
    $wnd.onpopstate = $entry(function(e) {
      if (e.state && e.state.historyToken) {
        that.@org.jboss.errai.ui.nav.client.local.pushstate.HistoryImplPushState::onPopState(Ljava/lang/String;)(e.state.historyToken);
      }
      if (oldHandler) {
        oldHandler();
      }
    });
  }-*/;

  /**
   * Called from native JavaScript when an old history state was popped.
   */
  private void onPopState(final String historyToken) {
    if (LogConfiguration.loggingIsEnabled()) {
      LOG.fine("Popped '" + historyToken + "'");
    }
    updateHistoryToken(historyToken);
    fireHistoryChangedImpl(token);
  }

  /**
   * Add the given token to the history using pushState.
   */
  private static native void pushState(final String token) /*-{
    var state = {
      historyToken : token
    };
    $wnd.history.pushState(state, $doc.title, token);
  }-*/;

  /**
   * Replace the given token in the history using replaceState.
   */
  private static native void replaceState(final String token) /*-{
    var state = {
      historyToken : token
    };
    $wnd.history.replaceState(state, $doc.title, token);
  }-*/;
  
  private native String encodeFragment(String fragment) /*-{
    // encodeURI() does *not* encode the '#' character.
    return encodeURI(fragment).replace("#", "%23");
  }-*/;
  
  private native String decodeFragment(String encodedFragment) /*-{
    // decodeURI() does *not* decode the '#' character.
    return decodeURI(encodedFragment.replace("%23", "#"));
  }-*/;

  /**
   * Fires the {@link ValueChangeEvent} to all handlers with the given tokens.
   */
  public void fireHistoryChangedImpl(String newToken) {
    ValueChangeEvent.fire(this, newToken);
  }
  
  /**
   * Fires the {@link ValueChangeEvent} to all handlers with the current token.
   */
  public void fireCurrentHistoryState() {
    ValueChangeEvent.fire(this, token);
  }
  
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlers.fireEvent(event);
  }

  /**
   * Adds a {@link ValueChangeEvent} handler to be informed of changes to the
   * browser's history stack.
   * 
   * @param handler the handler
   */
  public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<String> handler) {
    return handlers.addHandler(ValueChangeEvent.getType(), handler);
  }
  
  /**
   * Adds a new browser history entry. Calling this method will cause
   * {@link ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)}
   * to be called as well if and only if issueEvent is true.
   *
   * @param historyToken the token to associate with the new history item
   * @param issueEvent true if a
   *          {@link ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)}
   *          event should be issued
   */
  public final void newItem(String historyToken, boolean issueEvent) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(this.token)) {
      this.token = historyToken;
      nativeUpdate(historyToken);
      if (issueEvent) {
        fireHistoryChangedImpl(historyToken);
      }
    }
  }
}