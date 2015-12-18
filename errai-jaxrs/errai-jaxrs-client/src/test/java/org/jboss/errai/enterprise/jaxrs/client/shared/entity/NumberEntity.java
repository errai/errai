/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class NumberEntity {

  private Integer i;
  private List<Integer> is = new ArrayList<Integer>();

  private Long l;
  private List<Long> ls = new ArrayList<Long>();

  private Float f;
  private List<Float> fs = new ArrayList<Float>();

  private Double d;
  private List<Double> ds = new ArrayList<Double>();

  private Short s;
  private List<Short> ss = new ArrayList<Short>();

  public Integer getI() {
    return i;
  }

  public void setI(Integer i) {
    this.i = i;
  }

  public Long getL() {
    return l;
  }

  public void setL(Long l) {
    this.l = l;
  }

  public Float getF() {
    return f;
  }

  public void setF(Float f) {
    this.f = f;
  }

  public Double getD() {
    return d;
  }

  public void setD(Double d) {
    this.d = d;
  }

  public Short getS() {
    return s;
  }

  public void setS(Short s) {
    this.s = s;
  }

  public List<Integer> getIs() {
    return is;
  }

  public List<Long> getLs() {
    return ls;
  }

  public List<Float> getFs() {
    return fs;
  }

  public List<Double> getDs() {
    return ds;
  }

  public List<Short> getSs() {
    return ss;
  }

  public void setIs(List<Integer> is) {
    this.is = is;
  }

  public void setLs(List<Long> ls) {
    this.ls = ls;
  }

  public void setFs(List<Float> fs) {
    this.fs = fs;
  }

  public void setDs(List<Double> ds) {
    this.ds = ds;
  }

  public void setSs(List<Short> ss) {
    this.ss = ss;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((d == null) ? 0 : d.hashCode());
    result = prime * result + ((ds == null) ? 0 : ds.hashCode());
    result = prime * result + ((f == null) ? 0 : f.hashCode());
    result = prime * result + ((fs == null) ? 0 : fs.hashCode());
    result = prime * result + ((i == null) ? 0 : i.hashCode());
    result = prime * result + ((is == null) ? 0 : is.hashCode());
    result = prime * result + ((l == null) ? 0 : l.hashCode());
    result = prime * result + ((ls == null) ? 0 : ls.hashCode());
    result = prime * result + ((s == null) ? 0 : s.hashCode());
    result = prime * result + ((ss == null) ? 0 : ss.hashCode());
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
    NumberEntity other = (NumberEntity) obj;
    if (d == null) {
      if (other.d != null)
        return false;
    }
    else if (!d.equals(other.d))
      return false;
    if (ds == null) {
      if (other.ds != null)
        return false;
    }
    else if (!ds.equals(other.ds))
      return false;
    if (f == null) {
      if (other.f != null)
        return false;
    }
    else if (!f.equals(other.f))
      return false;
    if (fs == null) {
      if (other.fs != null)
        return false;
    }
    else if (!fs.equals(other.fs))
      return false;
    if (i == null) {
      if (other.i != null)
        return false;
    }
    else if (!i.equals(other.i))
      return false;
    if (is == null) {
      if (other.is != null)
        return false;
    }
    else if (!is.equals(other.is))
      return false;
    if (l == null) {
      if (other.l != null)
        return false;
    }
    else if (!l.equals(other.l))
      return false;
    if (ls == null) {
      if (other.ls != null)
        return false;
    }
    else if (!ls.equals(other.ls))
      return false;
    if (s == null) {
      if (other.s != null)
        return false;
    }
    else if (!s.equals(other.s))
      return false;
    if (ss == null) {
      if (other.ss != null)
        return false;
    }
    else if (!ss.equals(other.ss))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "NumberEntity [i=" + i + ", is=" + is + ", l=" + l + ", ls=" + ls + ", f=" + f + ", fs=" + fs + ", d=" + d
        + ", ds=" + ds + ", s=" + s + ", ss=" + ss + "]";
  }

}
