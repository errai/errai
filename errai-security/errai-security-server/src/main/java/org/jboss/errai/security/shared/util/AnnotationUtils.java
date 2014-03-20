package org.jboss.errai.security.shared.util;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class AnnotationUtils {

  /**
   * Merge roles from multiple {@link RestrictedAccess}.
   * 
   * @param accessRestrictions {@link RestrictedAccess} annotations with roles to be merged.
   * 
   * @return An array of unique role names.
   */
  public static String[] mergeRoles(final RestrictedAccess... accessRestrictions) {
    final Set<String> roles = new HashSet<String>();
    
    for (int i = 0; i < accessRestrictions.length; i++) {
      if (accessRestrictions[i] == null)
        continue;

      final String[] values = accessRestrictions[i].roles();
      for (int j = 0; j < values.length; j++) {
        roles.add(values[j]);
      }
    }
    
    return roles.toArray(new String[roles.size()]);
  }

}
