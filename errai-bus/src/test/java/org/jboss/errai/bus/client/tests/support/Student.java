/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Student extends Person {

  private int id;
  private String name;
  
  private final Integer nullInt = null;
  private final Float nullFloat = null;
  private final Double nullDouble = null;
  private final Short nullShort = null;
  private final Long nullLong = null;
  private final Byte nullByte = null;

  public Student() {}
  
  public Student(int id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getNullInt() {
    return nullInt;
  }

  public Float getNullFloat() {
    return nullFloat;
  }

  public Double getNullDouble() {
    return nullDouble;
  }

  public Short getNullShort() {
    return nullShort;
  }

  public Long getNullLong() {
    return nullLong;
  }

  public Byte getNullByte() {
    return nullByte;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Student)) return false;

    Student student = (Student) o;
    return id == student.id && !(name != null ? !name.equals(student.name) : student.name != null);
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Student [id=" + id + ", name=" + name + ", nullInt=" + nullInt + ", nullFloat=" + nullFloat
        + ", nullDouble=" + nullDouble + ", nullShort=" + nullShort + ", nullLong=" + nullLong + ", nullByte="
        + nullByte + "]";
  }
}
