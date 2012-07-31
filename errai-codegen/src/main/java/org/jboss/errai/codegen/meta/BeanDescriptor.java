package org.jboss.errai.codegen.meta;

import java.util.Set;

/**
 * @author Mike Brock
 */
public interface BeanDescriptor {
  public String getBeanName();
  /**
   * Get all bean properties associated with this bean type.
   *
   * @return a set of properties associated with this bean type.
   */
  public Set<String> getProperties();

  public MetaMethod getReadMethodForProperty(String propertyName);

  public MetaMethod getWriteMethodForProperty(String propertyName);
}
