package org.jboss.errai.marshalling.rebind.util;

import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingUtil {
  public static String getVarName(MetaClass clazz) {
    return getVarName(clazz.getFullyQualifiedName());
  }

  public static String getVarName(Class<?> clazz) {
    return getVarName(clazz.getName());
  }

  public static String getVarName(String clazz) {
    return clazz.replaceAll("\\.", "_");
  }
}
