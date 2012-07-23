package org.jboss.errai.demo.grocery.client.shared;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.demo.grocery.client.local.StoresWidget;

@Bindable @Entity
@EntityListeners(StoresWidget.StoreListener.class)
@NamedQuery(name="allStores", query="SELECT s FROM Store s WHERE 1 = 1")
public class Store {

  @Id @GeneratedValue
  private long id;

  private String name;

  @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
  private List<Department> departments = new ArrayList<Department>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Department> getDepartments() {
    return departments;
  }

  public void setDepartments(List<Department> departments) {
    this.departments = departments;
  }

  public long getId() {
    return id;
  }

  // TODO add location

  @Override
  public String toString() {
    return "Store [id=" + id + ", name=" + name + ", departments="
            + departments + "]";
  }
}
