package org.jboss.errai.ui.nav.client.local;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Used to extract page state values from the URL path.
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
   * {@link paramRegex} A regular expression that checks for parameters declared in a URL template. 
   * For example, in the URL {@code}/pageName/{id}/info{@code}, {@link paramRegex} would match 'id'.
   */
  static final String paramRegex = "\\{([^}]+)\\}";
  
  /**
   * {@link urlSafe} A
   * regular expression that checks if a typed URL only contains permitted URL characters.
   */
  public static final String urlSafe = "([A-Za-z0-9$\\-_.+!*'(),%]+)";

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
   * Uses the state map to construct the encoded web-safe URL for this pattern. Values in state that are not predefined 
   * path parameters (see {@link #getParamList()}) will be appended as key-value pairs.
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
        url = url.replace(toReplace, state.get(key).iterator().next());
      }
      else {
        throw new IllegalStateException("Path parameter list did not contain required parameter " + mr.getGroup(1));
      }
    }

    if (state.keySet().size() == paramList.size()) {
      return url;
    }
    
    StringBuilder urlBuilder = new StringBuilder(URL.encodePathSegment(url));
    urlBuilder.append(';');

    Iterator<Entry<String, String>> itr = state.entries().iterator();

    while (itr.hasNext()) {
      Entry<String, String> pageStateField = itr.next();
      if (!paramList.contains(pageStateField.getKey())) {
        urlBuilder.append(URL.encodePathSegment(pageStateField.getKey()));
        urlBuilder.append('=');
        urlBuilder.append(URL.encodePathSegment(pageStateField.getValue()));
        
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
}