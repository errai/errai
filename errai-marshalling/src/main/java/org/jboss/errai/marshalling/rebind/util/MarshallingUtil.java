package org.jboss.errai.marshalling.rebind.util;

import com.google.gwt.json.client.JSONString;
import org.jboss.errai.codegen.framework.Cast;
import org.jboss.errai.codegen.framework.Parameter;
import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.builder.impl.AnonymousClassStructureBuilderImpl;
import org.jboss.errai.codegen.framework.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.framework.util.Stmt;
import org.jboss.errai.marshalling.client.api.Marshaller;
import org.jboss.errai.marshalling.client.api.MarshallingContext;

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
