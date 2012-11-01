package org.jboss.errai.codegen.meta;

import java.util.Set;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
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
  
  public MetaClass getPropertyType(String propertyName);
}
