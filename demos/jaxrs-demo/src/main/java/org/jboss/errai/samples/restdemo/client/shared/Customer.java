package org.jboss.errai.samples.restdemo.client.shared;

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

/**
 * Simple customer entity
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ExposeEntity
public class Customer implements Serializable, Comparable<Customer> {
  private static final long serialVersionUID = 1L;

  private long id;
  private String firstName;
  private String lastName;
  private String postalCode;
  private Date lastChanged;

  public Customer() {}

  public Customer(String firstName, String lastName, String postalCode) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.postalCode = postalCode;
    this.lastChanged = new Date();
  }

  public Customer(long id, String firstName, String lastName, String postalCode) {
    this(firstName, lastName, postalCode);
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Date getLastChanged() {
    return lastChanged;
  }

  public void setLastChanged(Date lastChanged) {
    this.lastChanged = lastChanged;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @Override
  public String toString() {
    return "Customer [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", postalCode=" + postalCode
        + ", lastChanged=" + lastChanged + "]";
  }

  @Override
  public int compareTo(Customer customer) {
    return (int) (id - customer.id);
  }
}