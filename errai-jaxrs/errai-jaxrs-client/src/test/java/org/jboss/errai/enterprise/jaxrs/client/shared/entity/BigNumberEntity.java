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

package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class BigNumberEntity {

  private BigDecimal decimal;
  private BigInteger integer;

  public BigDecimal getDecimal() {
    return decimal;
  }

  public void setDecimal(BigDecimal decimal) {
    this.decimal = decimal;
  }

  public BigInteger getInteger() {
    return integer;
  }

  public void setInteger(BigInteger integer) {
    this.integer = integer;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((decimal == null) ? 0 : decimal.hashCode());
    result = prime * result + ((integer == null) ? 0 : integer.hashCode());
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
    BigNumberEntity other = (BigNumberEntity) obj;
    if (decimal == null) {
      if (other.decimal != null)
        return false;
    }
    else if (!decimal.equals(other.decimal))
      return false;
    if (integer == null) {
      if (other.integer != null)
        return false;
    }
    else if (!integer.equals(other.integer))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BigNumberEntity [decimal=" + decimal + ", integer=" + integer + "]";
  }

}
