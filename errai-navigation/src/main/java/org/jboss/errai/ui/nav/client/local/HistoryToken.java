package org.jboss.errai.ui.nav.client.local;

import java.util.Map;

import org.jboss.errai.common.client.api.Assert;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.gwt.http.client.URL;

/**
 * Represents the "history token" part of the location: the Errai UI Navigation
 * page name plus the names and values of its state parameters.
 * <p>
 * A history token consists of a mandatory page name followed by optional key=value pairs.
 * For example:
 * <pre>
 *     MyPage;key1=value1&key2=value2&multiKey=value1&multiKey=value2
 * </pre>
 * Keys are case-sensitive, so <tt>key</tt> and <tt>kEy</tt> are different keys.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class HistoryToken {

  private final String pageName;
  private final ImmutableMultimap<String, String> state;

  private HistoryToken(String pageName, ImmutableMultimap<String, String> state) {
    this.pageName = Assert.notNull(pageName);
    this.state = Assert.notNull(state);
  }

  /**
   * Returns a HistoryToken of the given page name and state encoded into the
   * given string.
   *
   * @param pageName
   *          the name of the page this History Token points to. Must not be null.
   * @return a HistoryToken with the given parameters. Never null.
   */
  public static HistoryToken of(String pageName, Multimap<String, String> state) {
    return new HistoryToken(pageName, ImmutableMultimap.copyOf(state));
  }

  /**
   * Returns a HistoryToken that represents the page name and state encoded into
   * the given string.
   *
   * @param token
   *          A history token string (must not be null). The format is described
   *          in the class-level documentation.
   * @return a HistoryToken. Never null.
   */
  public static HistoryToken parse(String token) {

    Builder<String, String> builder = ImmutableMultimap.builder();
    StringBuilder pageName = new StringBuilder();
    StringBuilder key = new StringBuilder();
    StringBuilder value = new StringBuilder();

    // sb is a state cursor in this little parser: it always points to one of the three
    // StringBuilders above; this is the one we're currently accumulating characters into.
    // you can also check the state of the parser by seeing which StringBuilder sb points at.
    StringBuilder sb = pageName;
    for (int i = 0, n = token.length(); i < n; i++) {
      char ch = token.charAt(i);
      if (ch == '&') {
        builder.put(URL.decodePathSegment(key.toString()), URL.decodePathSegment(value.toString()));
        key = new StringBuilder();
        value = new StringBuilder();
        sb = key;
      }
      else if (sb == pageName && ch == ';') {
        sb = key;
      }
      else if (ch == '=') {
        sb = value;
      }
      else {
        sb.append(ch);
      }
    }

    if (sb != pageName) {
      // we've got a key-value pair that still isn't in the map builder
      builder.put(URL.decodePathSegment(key.toString()), URL.decodePathSegment(value.toString()));
    }

    return new HistoryToken(URL.decodePathSegment(pageName.toString()), builder.build());
  }

  /**
   * Returns this history token's name and state parameters in the format that
   * can be parsed by {@link #parse(String)}.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(URL.encodePathSegment(pageName));
    sb.append(';');
    for (Map.Entry<String, String> entry : state.entries()) {
      sb.append(URL.encodePathSegment(entry.getKey()));
      sb.append("=");
      sb.append(URL.encodePathSegment(entry.getValue()));
    }
    return sb.toString();
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
