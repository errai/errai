package org.jboss.errai.samples.restdemo.client.shared;

import java.io.Serializable;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ExposeEntity
public class Customer implements Serializable {
  private static final long serialVersionUID = 1L;

  private long id;
  private String name;
  
  public Customer() {}
  
  public Customer(String name) {
    this.name = name;
  }
  
  public Customer(long id, String name) {
    this(name);
    this.id = id;
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
  public String toString() {
    return "Customer [id=" + id + ", name=" + name + "]";
  }
}