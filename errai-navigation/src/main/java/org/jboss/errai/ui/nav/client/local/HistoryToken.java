/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.api.Assert;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents the "history token" part of the location: the Errai UI Navigation page name plus the names and values of
 * its state parameters.
 * <p>
 * A history token consists of a mandatory page name followed by optional key=value pairs. For example:
 *
 * <pre>
 *     MyPage;key1=value1&key2=value2&multiKey=value1&multiKey=value2
 * </pre>
 *
 * Keys are case-sensitive, so <tt>key</tt> and <tt>kEy</tt> are different keys.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class HistoryToken {

  private final String pageName;
  private final ImmutableMultimap<String, String> state;
  private final URLPattern pattern;

  HistoryToken(String pageName, ImmutableMultimap<String, String> state, URLPattern pattern) {
    this.pageName = Assert.notNull(pageName);
    this.state = Assert.notNull(state);
    this.pattern = Assert.notNull(pattern);
  }

  /**
   * Returns the URL path for this HistoryToken, that can be parsed by {@link HistoryTokenFactory#parseURL(String)}
   * <p>
   * This URL path includes the application context.
   */
  @Override
  public String toString() {
    final String url = pattern.printURL(state);
    final String context = Navigation.getAppContext();

    if (!context.isEmpty() && !url.startsWith("/")) {
      return context + "/" + url;
    }
    else {
      return context + url;
    }
  }

  /**
   * Returns the page name. Guaranteed non-null.
   */
  public String getPageName() {
    return pageName;
  }

  /**
   * Returns an immutable map of the state information in this history token.
   */
  public Multimap<String, String> getState() {
    return state;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pageName == null) ? 0 : pageName.hashCode());
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HistoryToken other = (HistoryToken) obj;
    if (pageName == null) {
      if (other.pageName != null)
        return false;
    }
    else if (!pageName.equals(other.pageName))
      return false;
    if (state == null) {
      if (other.state != null)
        return false;
    }
    else if (!state.equals(other.state))
      return false;
    return true;
  }

}
