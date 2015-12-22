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

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Used to extract page state values from the URL path.
 * This class generates a URL where the page state values are appropriately encoded for parsing. Thus the URL must be
 * appropriately decoded. See @see URLPattern#decodeParsingCharacters() below.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */

public class URLPattern {
  private final List<String> paramList;
  private final RegExp regex;
  private final String urlTemplate;

  /**
   * A regular expression that checks for parameters declared in a URL template.
   * For example, in the URL {@code}/pageName/{id}/info{@code}, paramRegex would match 'id'.
   */
  static final String paramRegex = "\\{([^}]+)\\}";

  /**
   * A regular expression that we use to match a path parameter value. Since the value can contain any characters,
   * this regex matches everything. Any characters that we use for parsing will later be encoded.
   */
  public static final String urlSafe = "([^/]+)";

  public URLPattern(RegExp regex, List<String> paramList, String urlTemplate) {
    this.regex = regex;
    this.paramList = paramList;
    this.urlTemplate = urlTemplate;
  }

  /**
   * @return A list of path parameter key names. Never null.
   */
  public List<String> getParamList() {
    return paramList;
  }

  /**
   * @return The regular expression used to match URLs.
   */
  public RegExp getRegex() {
    return regex;
  }

  /**
   * @param url
   *          This URL path should not contain the application context and should not contain a leading slash.
   * @return True if this pattern matches the given URL path
   */
  public boolean matches(String url) {
    return regex.test(url);
  }

  /**
   * Uses the state map to construct the encoded URL for this pattern. Values in state that are not predefined
   * path parameters (see {@link #getParamList()}) will be appended as key-value pairs.
   * Note that this method only encodes the URL in a format that can be parsed by {@see URLPatternMatcher#parseURL()
   * parseURL()}.
   *
   * @param state
   * @throws IllegalStateException
   *           If a path parameter is missing from the given state map.
   * @return The constructed URL path without the application context.
   */
  public String printURL(ImmutableMultimap<String, String> state) {
    RegExp re = RegExp.compile(paramRegex, "g");
    String url = this.urlTemplate;

    MatchResult mr;

    while ((mr = re.exec(this.urlTemplate)) != null) {
      String toReplace = mr.getGroup(0);
      String key = mr.getGroup(1);
      if (toReplace.contains(key)) {
        // Encode all the characters we use to parse URLs.
        String encodedValue = URLPattern.encodeParsingCharacters(state.get(key).iterator().next());
        url = url.replace(toReplace, encodedValue);
      }
      else {
        throw new IllegalStateException("Path parameter list did not contain required parameter " + mr.getGroup(1));
      }
    }

    if (state.keySet().size() == paramList.size()) {
      return url;
    }

    StringBuilder urlBuilder = new StringBuilder(url);
    urlBuilder.append(';');

    Iterator<Entry<String, String>> itr = state.entries().iterator();

    while (itr.hasNext()) {
      Entry<String, String> pageStateField = itr.next();
      if (!paramList.contains(pageStateField.getKey())) {
        // Encode the parts of the value that may interfere with parsing
        urlBuilder.append(URLPattern.encodeParsingCharacters(pageStateField.getKey()));
        urlBuilder.append('=');
        urlBuilder.append(URLPattern.encodeParsingCharacters(pageStateField.getValue()));

        if (itr.hasNext())
          urlBuilder.append('&');
      }

    }
    return urlBuilder.toString();
  }

  @Override
  public String toString() {
    return urlTemplate;
  }

  /**
   * Replaces the characters used for parsing with their encoded equivalents.
   * The characters ";", "/", "&" and "=" are used in URLPattern and URLPatternMatcher to parse the given URL.
   * Hence any occurrences of these characters in the actual page state values are 'escaped' so that it doesn't
   * interfere with our URL parsing.
   *
   * @param plainValue The string that may contain any parsing characters.
   * @return The same value with the appropriate characters 'escaped'.
   */
  static String encodeParsingCharacters(String plainValue) {
    return plainValue.replaceAll("%", "%25").replaceAll(";","%3B").replaceAll("/","%2F").replaceAll("&", "%26")
             .replaceAll("=", "%3D");
  }

  /**
   * This method is the converse of {@link URLPattern#encodeParsingCharacters}.
   * It 'un-escapes' all the parsing characters encoded by {@link URLPattern#encodeParsingCharacters}.
   *
   * @param escapedValue The string where the characters ";", "/", "&" and "=" have been encoded.
   * @return The same string where all the encoded values are replaced by the actual characters.
   */
  static String decodeParsingCharacters(String escapedValue) {
    return escapedValue.replaceAll("%3B",";").replaceAll("%2F", "/").replaceAll("%26", "&")
             .replaceAll("%3D", "=").replace("%25", "%");
  }
}
