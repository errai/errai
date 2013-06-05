package org.jboss.errai.jpa.sync.test.client.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class ParentEntity implements Cloneable {

  @Id @GeneratedValue
  private int id;

  private String string;
  private Integer integer;

  @OneToMany(mappedBy="parent")
  private List<ChildEntity> children = new ArrayList<ChildEntity>();

  public ParentEntity() {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public List<ChildEntity> getChildren() {
    return children;
  }

  public void setChildren(List<ChildEntity> children) {
    this.children = children;
  }

  public void addChild(ChildEntity child) {
    child.setParent(this);
    children.add(child);
  }

  @Override
  public String toString() {
    return "ParentEntity [id=" + id + ", string=" + string + ", integer=" + integer + ", children=" + children + "]";
  }
}
