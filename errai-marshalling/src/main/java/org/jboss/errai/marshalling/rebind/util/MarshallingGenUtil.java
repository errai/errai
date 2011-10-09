package org.jboss.errai.marshalling.rebind.util;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.util.GenUtil;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingGenUtil {
  public static String getVarName(MetaClass clazz) {
    return clazz.isArray()
            ? getArrayVarName(clazz.getOuterComponentType().getFullyQualifiedName())
            + "_D" + GenUtil.getArrayDimensions(clazz)
            : getVarName(clazz.getFullyQualifiedName());
  }

  public static String getVarName(Class<?> clazz) {
    return getVarName(MetaClassFactory.get(clazz));
  }

  public static String getArrayVarName(String clazz) {
    return "arrayOf_" + clazz.replaceAll("\\.", "_");
  }
  
  public static String getVarName(String clazz) {
    return clazz.replaceAll("\\.", "_");
  }
}
