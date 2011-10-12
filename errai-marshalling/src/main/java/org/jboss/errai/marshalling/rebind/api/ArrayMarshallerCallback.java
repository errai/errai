package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface ArrayMarshallerCallback {
  public Statement marshal(MetaClass type, Statement value);
  public Statement demarshall(MetaClass type, Statement value);
}
