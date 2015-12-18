package org.jboss.errai.jpa.client.local;

import com.google.gwt.json.client.JSONObject;

/**
 * First try at an interface that describes a query's predicate with regards to
 * a single object type.
 * <p>
 * <b>Future consideration:</b> This interface must be extended or completely
 * reworked to accommodate indexing (if indexing turns out to be important in
 * the client data store).
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface EntityJsonMatcher {

  /**
   * Tests if the given JSON object as accepted by this matcher.
   *
   * @param candidate
   *          The JSON representation of a Java object to be tested. It is
   *          assumed to be of the same Java type that this matcher cares about.
   * @return true if this matcher accepts the candidate; false otherwise.
   */
  boolean matches(JSONObject candidate);

}
