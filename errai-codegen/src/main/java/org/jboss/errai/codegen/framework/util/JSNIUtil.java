package org.jboss.errai.codegen.framework.util;

import org.jboss.errai.codegen.framework.meta.MetaField;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.MetaParameter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class JSNIUtil {
  public static String fieldAccess(MetaField field) {
    return "instance.@" + field.getDeclaringClass().getFullyQualifiedName() + "::"
            + field.getName();
  }

  public static String methodAccess(MetaMethod method) {
    StringBuilder buf = new StringBuilder(
            (method.getReturnType().isVoid() ? "" : "return ") +
                    "instance.@"
                    + method.getDeclaringClass().getFullyQualifiedName() + "::"
                    + method.getName() + "(");

    for (MetaParameter parm : method.getParameters()) {
      buf.append(parm.getType().getInternalName());
    }
    buf.append(")(");

    int length = method.getParameters().length;

    for (int i = 0; i < length; i++) {
      buf.append("a").append(i);
      if (i + 1 < length) buf.append(",");
    }
    buf.append(")");

    return buf.toString();
  }
}
