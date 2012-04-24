package org.jboss.errai.enterprise.client.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * @author Mike Brock
 */
public final class CDIEventTypeLookup {
  private Map<String, Collection<String>> typeLookup = new HashMap<String, Collection<String>>();

  private CDIEventTypeLookup() {
  }

  private final static CDIEventTypeLookup typeLookupSingleton = new CDIEventTypeLookup();

  public static CDIEventTypeLookup get() {
    return typeLookupSingleton;
  }

  /**
   * Returns a defensively copied, unmodifiable Map of all sub-types to their supertypes.
   *
   * @return a defensively copied, unmodifiable Map of all sub-types to their supertypes.
   */
  public Map<String, Collection<String>> getTypeLookupMap() {
    // build a defensive copy of the type lookup map.

    Map<String, Collection<String>> newLookupMap = new HashMap<String, Collection<String>>();
    for (Map.Entry<String, Collection<String>> entry : typeLookup.entrySet()) {
      newLookupMap.put(entry.getKey(), Collections.unmodifiableSet(new LinkedHashSet<String>(entry.getValue())));
    }

    return unmodifiableMap(newLookupMap);
  }

  public void addLookup(final String subtype, final String superType) {
    Collection<String> superTypes = typeLookup.get(subtype);
    if (superTypes == null) {
      typeLookup.put(subtype, superTypes = new LinkedHashSet<String>());
    }
    superTypes.add(superType);
  }
}
