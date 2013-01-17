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
import org.jboss.errai.demo.grocery.client.local.StoresPage;

@Bindable @Entity
@EntityListeners(StoresPage.StoreListener.class)
@NamedQuery(name="allStores", query="SELECT s FROM Store s ORDER BY s.name")
public class Store {

  @Id @GeneratedValue
  private long id;

  private String name;
  private String address;

  private double latitude;
  private double longitude;

  @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
  private List<Department> departments = new ArrayList<Department>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
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

  @Override
  public String toString() {
    return "Store [id=" + id + ", name=" + name + ", address=" + address + ", latitude=" + latitude + ", longitude="
            + longitude + ", departments=" + departments + "]";
  }
}
