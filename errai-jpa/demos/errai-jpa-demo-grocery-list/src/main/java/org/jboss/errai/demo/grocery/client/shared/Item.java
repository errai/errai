package org.jboss.errai.demo.grocery.client.shared;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.demo.grocery.client.local.ItemListPage;

@Bindable
@Entity
@EntityListeners(ItemListPage.ItemListener.class)
@NamedQuery(name="allItemsByName", query="SELECT i FROM Item i ORDER BY i.name")
public class Item {

  @Id @GeneratedValue
  private long id;

  private String name;

  @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH})
  private Department department;

  private String comment;

  @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH})
  private User addedBy;

  public User getAddedBy() {
    return addedBy;
  }
  public void setAddedBy(User addedBy) {
    this.addedBy = addedBy;
  }
  public Date getAddedOn() {
    return addedOn;
  }
  public void setAddedOn(Date addedOn) {
    this.addedOn = addedOn;
  }

  private Date addedOn;

  public long getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Department getDepartment() {
    return department;
  }
  public void setDepartment(Department department) {
    this.department = department;
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
    result = prime * result + (int) (id ^ (id >>> 32));
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
    if (id != other.id)
      return false;
    return true;
  }
  @Override
  public String toString() {
    return "Item [id=" + id + ", name=" + name + ", comment=" + comment + "]";
  }
}
