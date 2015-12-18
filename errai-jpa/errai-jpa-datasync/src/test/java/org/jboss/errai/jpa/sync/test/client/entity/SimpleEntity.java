/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.sync.test.client.entity;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

@Entity
@NamedQueries({
    @NamedQuery(name = "allSimpleEntities", query = "SELECT se FROM SimpleEntity se"),
    @NamedQuery(name = "simpleEntitiesByIdAndString",
         query = "SELECT se FROM SimpleEntity se WHERE se.id = :id AND se.string = :string AND :literal IS NOT NULL") })
public class SimpleEntity implements Cloneable {

  @Id
  @GeneratedValue
  private Long id;

  @Version
  private int version;

  private String string;
  private Integer integer;

  private Timestamp date;

  public SimpleEntity() {}

  public SimpleEntity(SimpleEntity copyMe) {
    id = copyMe.id;
    string = copyMe.string;
    integer = copyMe.integer;
    date = copyMe.date;
  }

  public Long getId() {
    return id;
  }

  public int getVersion() {
    return version;
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

  public Timestamp getDate() {
    return date;
  }

  public void setDate(Timestamp date) {
    this.date = date;
  }

  /**
   * Sets the ID on the given SimpleEntity.
   * <p>
   * This static method is used instead of a setId() method because I want to be sure the datasync
   * system still works when there's no actual ID setter method.
   * 
   * @param instance
   *          the SimpleEntity instance whose ID to set
   * @param id
   *          the new ID value
   */
  public static void setId(SimpleEntity instance, Long id) {
    instance.id = id;
  }

  @Override
  public String toString() {
    // Warning: tests rely on this toString() fully representing the state of the object
    // version should not be included here because it changes asymmetrically
    return "SimpleEntity [id=" + id + ", string=" + string + ", integer=" + integer + ", date="
        + (date == null ? null : date.toString()) + "]";
  }

}
