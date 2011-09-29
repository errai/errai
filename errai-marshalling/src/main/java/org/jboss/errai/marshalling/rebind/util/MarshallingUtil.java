package org.jboss.errai.marshalling.rebind.util;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingUtil {
  public static String getVarName(Class<?> clazz) {
    return getVarName(clazz.getName());
  }

  public static String getVarName(String clazz) {
    return clazz.replaceAll("\\.", "_");
  }
}
