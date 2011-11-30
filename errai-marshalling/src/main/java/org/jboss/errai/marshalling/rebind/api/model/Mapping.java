package org.jboss.errai.marshalling.rebind.api.model;

import org.jboss.errai.codegen.framework.meta.MetaClass;

/**
 * @author Mike Brock
 */
public interface Mapping {
  public String getKey();
  public MetaClass getType();
}
