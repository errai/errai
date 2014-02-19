package org.jboss.errai.security.shared.util;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.security.shared.RequireRoles;

public class AnnotationUtils {

  public static Annotation[] mergeAnnotations(final Annotation[] first, final Annotation[] second) {
    final Map<Class<? extends Annotation>, Annotation> retMap = new HashMap<Class<? extends Annotation>, Annotation>();

    for (int i = 0; i < first.length; i++) {
      retMap.put(first[i].annotationType(), first[i]);
    }

    for (int i = 0; i < second.length; i++) {
      final Annotation annotation = retMap.get(second[i].annotationType());
      if (annotation == null) {
        retMap.put(second[i].annotationType(), second[i]);
      }
    }
    
    return retMap.values().toArray(new Annotation[retMap.size()]);
  }
  
  public static String[] mergeRoles(final RequireRoles... requireRoles) {
    final Set<String> roles = new HashSet<String>();
    
    for (int i = 0; i < requireRoles.length; i++) {
      if (requireRoles[i] == null)
        continue;

      final String[] values = requireRoles[i].value();
      for (int j = 0; j < values.length; j++) {
        roles.add(values[j]);
      }
    }
    
    return roles.toArray(new String[roles.size()]);
  }

}
