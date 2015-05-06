/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.client;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable
public class TestModelWithBindableTypeList {

  private String id;
  
  public TestModelWithBindableTypeList() {}
  
  public TestModelWithBindableTypeList(String id) {
    this.id = id;
  }
  
  private List<TestModelWithBindableTypeList> list = new ArrayList<TestModelWithBindableTypeList>();
  
  public List<TestModelWithBindableTypeList> getList() {
    return list;
  }

  public void setList(List<TestModelWithBindableTypeList> list) {
    this.list = list;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((list == null) ? 0 : list.hashCode());
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
    TestModelWithBindableTypeList other = (TestModelWithBindableTypeList) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (list == null) {
      if (other.list != null)
        return false;
    }
    else if (!list.equals(other.list))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "TestModelWithBindableTypeList [id=" + id + ", list=" + list + "]";
  }
  
}
