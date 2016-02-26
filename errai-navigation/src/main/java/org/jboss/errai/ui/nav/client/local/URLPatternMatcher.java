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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.errai.ui.nav.client.local.api.PageNotFoundException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Used to match URLs typed in by the user to the correct {@link Page#path()}
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class URLPatternMatcher {
  /**
   * Used to look up page names using a given URL pattern.
   */
  private BiMap<URLPattern, String> pageMap = HashBiMap.create();
  private String defaultPageName;

  /**
   * Adds the allowed URL template as specified in the {@link Page#path()} by the developer.
   *
   * @param urlTemplate
   *          The page URL pattern specified in the {@link Page#path()}.
   * @param pageName
   *          The name of the page.
   */
  public void add(String urlTemplate, String pageName) {
    URLPattern urlPattern = generatePattern(urlTemplate);
    pageMap.put(urlPattern, pageName);
  }


 /**
  * Generates a {@link URLPattern} from a {@link Page#path()}
  * @param urlTemplate The {@link Page#path()}
  * @return A {@link URLPattern} used to match URLs
  */
  public static URLPattern generatePattern(String urlTemplate) {
    RegExp regex = RegExp.compile(URLPattern.paramRegex, "g");
    List<String> paramList = new ArrayList<String>();

    MatchResult mr;
    StringBuilder sb = new StringBuilder();

    // Ensure matching at beginning of line
    sb.append("^");
    // Match patterns with or without leading slash
    sb.append("/?");
    // Since we handle an optional leading slash, strip it from the url template.
    if (urlTemplate.startsWith("/")) {
      urlTemplate.substring(1);
    }

    int endOfPreviousPattern = 0;
    int startOfNextPattern = 0;

    while ((mr = regex.exec(urlTemplate)) != null) {
      addParamName(paramList, mr);
      startOfNextPattern = mr.getIndex();

      // Append any string literal that may occur in the URL path
      // before the next parameter.
      sb.append(urlTemplate, endOfPreviousPattern, startOfNextPattern);

      // Append regex for matching the parameter value
      sb.append(URLPattern.urlSafe);

      endOfPreviousPattern = regex.getLastIndex();
    }

    // Append any remaining trailing string literals
    sb.append(urlTemplate, endOfPreviousPattern, urlTemplate.length());

    // Ensure matching at end of line
    sb.append("$");

    return new URLPattern(RegExp.compile(sb.toString()), paramList, urlTemplate);
  }

  private static void addParamName(List<String> paramList, MatchResult mr) {
    paramList.add(mr.getGroup(1));
  }

/**
 * Creates a {@link HistoryToken} by parsing a URL path. This path should never include the application context.
 */
  public HistoryToken parseURL(String url) {
    Builder<String, String> mapBuilder = ImmutableMultimap.builder();
    String keyValuePairs, pageInfo;

    int indexOfSemicolon = url.indexOf(';');

    if (indexOfSemicolon > 0) {
      pageInfo = url.substring(0, indexOfSemicolon);
      keyValuePairs = url.substring(indexOfSemicolon + 1);
    }
    else {
      pageInfo = url;
      keyValuePairs = null;
    }

    String pageName = parseValues(pageInfo, mapBuilder);
    if (pageName == null)
      throw new PageNotFoundException("Invalid URL \"" + URLPattern.decodeParsingCharacters(url) + "\" could not be mapped to any page.");

    if (keyValuePairs != null) {
      parseKeyValuePairs(keyValuePairs, mapBuilder);
    }

    Multimap<String, String> state = mapBuilder.build();
    return new HistoryToken(pageName, ImmutableMultimap.copyOf(state),
                             getURLPattern(pageName));
  }

  private String parseValues(String rawURIPath, Builder<String, String> builder) {
    String pageName = getPageName(rawURIPath);
    if (pageName == null)
      return null;

    URLPattern pattern = getURLPattern(pageName);
    if (pattern.getParamList().size() == 0)
      return pageName;

    MatchResult mr = pattern.getRegex().exec(rawURIPath);
    for (int keyIndex = 0; keyIndex < pattern.getParamList().size(); keyIndex++) {
      builder.put(URLPattern.decodeParsingCharacters(pattern.getParamList().get(keyIndex)), URLPattern
             .decodeParsingCharacters(mr.getGroup(keyIndex + 1)));
    }
    return pageName;
  }

  private void parseKeyValuePairs(String rawKeyValueString, Builder<String, String> builder) {
    StringBuilder key = new StringBuilder();
    StringBuilder value = new StringBuilder();

    // sb is a state cursor in this little parser: it always points to one of the
    // StringBuilders above; this is the one we're currently accumulating characters into.
    // you can also check the state of the parser by seeing which StringBuilder sb points at.
    StringBuilder sb = key;
    for (int i = 0, n = rawKeyValueString.length(); i < n; i++) {
      char ch = rawKeyValueString.charAt(i);
      if (ch == '&') {
        builder.put(URLPattern.decodeParsingCharacters(key.toString()), URLPattern.decodeParsingCharacters(value.toString()));
        key = new StringBuilder();
        value = new StringBuilder();
        sb = key;
      }
      else if (ch == '=') {
        sb = value;
      }
      else {
        sb.append(ch);
      }
    }
    // we've got a key-value pair that still isn't in the map builder
    builder.put(URLPattern.decodeParsingCharacters(key.toString()), URLPattern.decodeParsingCharacters(value.toString()));
  }

  /**
   * Declares the default page to be matched against the empty string pattern.
   * @param defaultPage Never null. Must match a page that has already been added with {@link #add(String, String)}
   */
  public void setAsDefaultPage(String defaultPage) {
    URLPattern urlPattern = getURLPattern(defaultPage);
    if (urlPattern == null)
      throw new IllegalArgumentException("Page " + defaultPage + " must be added to URLPatternMatcher before it can be set as Default Page.");

    if (urlPattern.getParamList().size() > 0)
      throw new IllegalArgumentException("Cannot set a default page that has path parameters.");

    this.defaultPageName = defaultPage;
  }

  /**
   * @param pageName The name of the page corresponding to the {@link URLPattern}
   * @return The {@link URLPattern} for the given page name.
   */
  public URLPattern getURLPattern(String pageName) {
    return pageMap.inverse().get(pageName);
  }

  /**
   * @return The name of the page to which the given user-entered URL corresponds.
   */
  public String getPageName(String typedURL) {
    if (typedURL.equals("")) {
      return this.defaultPageName;
    }
    for (Entry<URLPattern, String> urlMatcher : pageMap.entrySet()) {
      if (urlMatcher.getKey().matches(typedURL)) {
        return urlMatcher.getValue();
      }
    }
    return null;

  }

}
