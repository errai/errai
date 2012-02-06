/*
 * Copyright 2011 JBoss, a division of Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.samples.restdemo.client.shared;

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Simple customer entity
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Portable
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