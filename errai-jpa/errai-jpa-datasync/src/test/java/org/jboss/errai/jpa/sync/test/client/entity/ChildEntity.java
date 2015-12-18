package org.jboss.errai.jpa.sync.test.client.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class ChildEntity implements Cloneable {

  @Id @GeneratedValue
  private int id;

  @ManyToOne
  private ParentEntity parent;

  private String string;
  private Integer integer;

  public ChildEntity() {
  }

  public ChildEntity(String string, Integer integer) {
    this.string = string;
    this.integer = integer;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ParentEntity getParent() {
    return parent;
  }

  public void setParent(ParentEntity parent) {
    this.parent = parent;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public Integer getInteger() {
    return integer;
  }

  public void setInteger(Integer integer) {
    this.integer = integer;
  }

  @Override
  public String toString() {
    return "ChildEntity [id=" + id + ", string=" + string + ", integer=" + integer + "]";
  }
}
