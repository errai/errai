package org.jboss.errai.demo.grocery.client.shared;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

/**
 * Represents an item that is in or has been in the grocery list. This is used
 * mostly as a source of suggestions for autocompletion.
 * 
 * @author jfuerth
 */
@Entity
@NamedQuery(name="allHistoricalItems", query="SELECT i FROM HistoricalItem i")
public class HistoricalItem {

  @Id @GeneratedValue
  private long id;

  private String name;

  @ManyToOne(cascade={CascadeType.PERSIST, CascadeType.REFRESH})
  private Department department;

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
    HistoricalItem other = (HistoricalItem) obj;
    if (id != other.id)
      return false;
    return true;
  }
  @Override
  public String toString() {
    return "HistoricalItem [id=" + id + ", name=" + name + ", department=" + department + "]";
  }
}
