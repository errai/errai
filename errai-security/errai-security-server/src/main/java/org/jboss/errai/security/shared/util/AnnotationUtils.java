package org.jboss.errai.security.shared.util;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.security.shared.api.annotation.RestrictAccess;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class AnnotationUtils {

  /**
   * Merge roles from multiple {@link RestrictAccess}.
   * 
   * @param requireRoles {@link RestrictAccess} annotations with roles to be merged.
   * 
   * @return An array of unique role names.
   */
  public static String[] mergeRoles(final RestrictAccess... requireRoles) {
    final Set<String> roles = new HashSet<String>();
    
    for (int i = 0; i < requireRoles.length; i++) {
      if (requireRoles[i] == null)
        continue;

      final String[] values = requireRoles[i].roles();
      for (int j = 0; j < values.length; j++) {
        roles.add(values[j]);
      }
    }
    
    return roles.toArray(new String[roles.size()]);
  }

}
