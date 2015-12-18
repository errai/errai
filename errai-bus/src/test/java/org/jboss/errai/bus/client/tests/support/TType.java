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

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class TType {
  private String fieldOne;
  private String fieldTwo;
  private Date startDate;
  private Date endDate;
  private Boolean active;

  public String getFieldOne() {
    return fieldOne;
  }

  public void setFieldOne(String fieldOne) {
    this.fieldOne = fieldOne;
  }

  public String getFieldTwo() {
    return fieldTwo;
  }

  public void setFieldTwo(String fieldTwo) {
    this.fieldTwo = fieldTwo;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TType sType = (TType) o;

    if (active != null ? !active.equals(sType.active) : sType.active != null) return false;
    if (endDate != null ? !endDate.equals(sType.endDate) : sType.endDate != null) return false;
    if (fieldOne != null ? !fieldOne.equals(sType.fieldOne) : sType.fieldOne != null) return false;
    if (fieldTwo != null ? !fieldTwo.equals(sType.fieldTwo) : sType.fieldTwo != null) return false;
    if (startDate != null ? !startDate.equals(sType.startDate) : sType.startDate != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fieldOne != null ? fieldOne.hashCode() : 0;
    result = 31 * result + (fieldTwo != null ? fieldTwo.hashCode() : 0);
    result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
    result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
    result = 31 * result + (active != null ? active.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return fieldOne + "|" + fieldTwo + "|" + startDate + "|" + endDate + "|" + active;
  }
}
