package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Simple test entity.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
public class Entity implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private long id;
  private String name;

  public Entity() {
    
  }
  
  public Entity(long id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Entity other = (Entity) obj;
    if (id != other.id)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Entity [id=" + id + ", name=" + name + "]";
  }
}
