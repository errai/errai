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

import com.google.gwt.core.client.GWT;
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

  private String currentPrefix;

  private HandlerManager handlers = new HandlerManager(null);
  private String token = "";

  public boolean init(final String pathPrefix) {
    // read default path prefix
    final PathProvider pathProvider = ((pathPrefix == null || pathPrefix.isEmpty())
            ? GWT.<PathProvider>create(PathProvider.class)
            : new PathProvider() {
                @Override
                public String getPathPrefix() {
                    return pathPrefix;
                }
            });

    this.currentPrefix = pathProvider.getPathPrefix();

    // initialize HistoryImpl with the current path
    updateHistoryToken(Window.Location.getPath() + Window.Location.getQueryString());
    // initialize the empty state with the current history token
    nativeUpdate(token, null);
    // initialize the popState handler
    initPopStateHandler();

    return true;
  }

  private String prepareToken(String historyToken) {
      String newPushStateToken = CodeServerParameterHelper.append(encodeFragment(historyToken));
      if (!newPushStateToken.startsWith(currentPrefix)) {
        newPushStateToken = currentPrefix + newPushStateToken;
      }
      return newPushStateToken;
  }

  private void nativeUpdate(final String historyToken, final String title) {
    String newPushStateToken = prepareToken(historyToken);
    pushState(newPushStateToken, title);
    if (LogConfiguration.loggingIsEnabled()) {
      LOG.fine("Pushed '" + newPushStateToken + "' (" + historyToken + ")");
    }
  }

  private void nativeReplace(final String historyToken, final String title) {
    String newReplaceStateToken = prepareToken(historyToken);
    replaceState(newReplaceStateToken, title);
    if (LogConfiguration.loggingIsEnabled()) {
      LOG.fine("Replaced '" + newReplaceStateToken + "' (" + historyToken + ")");
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
    if (token.startsWith(currentPrefix)) {
        token = token.substring(currentPrefix.length());
    }

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
        that.@com.versonix.common.client.ui.browserhistory.pushstate.HistoryImplPushState::onPopState(Ljava/lang/String;)(e.state.historyToken);
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
  private static native void pushState(final String token, final String title) /*-{
    var s = (title === null || title === '') ? $doc.title : title;
    var state = {
      historyToken : token,
      historyTitle : s
    };
    $wnd.history.pushState(state, s, token);
  }-*/;

  private static native void replaceState(final String token, final String title) /*-{
    var s = (title === null || title === '') ? $doc.title : title;
    var state = {
      historyToken : token,
      historyTitle : s
    };
    $wnd.history.replaceState(state, s, token);
  }-*/;

  private static native String getCurrentHistoryToken() /*-{
      var state = $wnd.history.state;
      if (state) return state.historyToken;
      else return null;
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
  @Override
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
  public final void newItem(String historyToken, String title, boolean issueEvent) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(this.token)) {
      this.token = historyToken;
      nativeUpdate(historyToken, title);
      if (issueEvent) {
        fireHistoryChangedImpl(historyToken);
      }
    }
  }

  public final void replaceItem(String historyToken, String title, boolean issueEvent) {
    historyToken = (historyToken == null) ? "" : historyToken;
    if (!historyToken.equals(this.token)) {
      this.token = historyToken;
      nativeReplace(historyToken, title);
      if (issueEvent) {
        fireHistoryChangedImpl(historyToken);
      }
    }
  }

  public String getCurrentToken() {
      String s = getCurrentHistoryToken();
      if (s == null || s.isEmpty()) return s;
      // could be like: /hist-productPkgTypesSearch?gwt.codesvr=127.0.0.1:9997
      int j = s.charAt(0) == '/' ? 1 : 0;
      int i = s.indexOf('?');
      if (i >= 0) s = s.substring(j, i);
      else if (j > 0) s = s.substring(j);
      return s;
  }
}
