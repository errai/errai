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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * All HistoryToken instances are produced by this class.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
@ApplicationScoped
public class HistoryTokenFactory {
  private final URLPatternMatcher patternMatcher;

  // For proxying
  public HistoryTokenFactory() {
    patternMatcher = null;
  }

  @Inject
  public HistoryTokenFactory(URLPatternMatcher upm) {
    this.patternMatcher = upm;
  }

  /**
   * This can be used to create a HistoryToken when navigating by page name.
   *
   * @param pageName
   *          The name of the page. Never null.
   * @param state
   *          The map of {@link PageState} keys and values. Never null.
   * @return A HistoryToken with the parsed URL matching information.
   */
  public HistoryToken createHistoryToken(String pageName, Multimap<String, String> state) {
    URLPattern pattern = patternMatcher.getURLPattern(pageName);
    return new HistoryToken(pageName, ImmutableMultimap.copyOf(state), pattern);
  }

  /**
   * This can be used to generate a HistoryToken from a URL path
   *
   * @param url
   *          The typed URL path. If the browser is pushstate-enabled, this will be the URI path, otherwise it will be
   *          the fragment identifier.
   * @return A HistoryToken with the parsed URL matching information.
   */
  public HistoryToken parseURL(String url) {
    // Remove the leading slash from the context and the URL for pushstate/non-pushstate URL compatibility.
    String context = Navigation.getAppContext();
    if ((!(context.equals(""))) && (context.startsWith("/")))
      context = context.substring(1);

    if (url.startsWith("/")) {
      url = url.substring(1);
    }

    if (url.startsWith(context)) {
      url = url.substring(context.length());
    }

    return patternMatcher.parseURL(url);
  }
}
