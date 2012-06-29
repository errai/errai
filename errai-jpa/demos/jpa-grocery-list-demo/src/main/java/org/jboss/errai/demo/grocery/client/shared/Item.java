package org.jboss.errai.demo.grocery.client.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Item {

  @Id @GeneratedValue
  private long id;

  @ManyToOne
  private Department department;

  private String name;
  private String comment;

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getComment() {
    return comment;
  }
  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((comment == null) ? 0 : comment.hashCode());
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
    Item other = (Item) obj;
    if (comment == null) {
      if (other.comment != null)
        return false;
    }
    else if (!comment.equals(other.comment))
      return false;
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
    return "Item [id=" + id + ", name=" + name + ", comment=" + comment + "]";
  }
}
