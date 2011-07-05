package org.jboss.errai.ioc.rebind.ioc.codegen.util;

import org.jboss.errai.ioc.rebind.ioc.codegen.meta.MetaField;

import java.lang.reflect.Field;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JSNIUtil {
  public static String fieldAccess(MetaField field) {
    return "instance.@" + field.getDeclaringClass().getFullyQualifiedName() + "::"
            + field.getName();
  }
}
