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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
@Portable
public class Koron {
   private List<Integer> someList = new ArrayList<Integer>();
   private List<Integer> sameList = someList;
   private List<Integer> otherList = new ArrayList<Integer>();

  public List<Integer> getSomeList() {
    return someList;
  }

  public List<Integer> getSameList() {
    return sameList;
  }

  public List<Integer> getOtherList() {
    return otherList;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Koron)) return false;

    Koron koron = (Koron) o;

    return !(otherList != null ? !otherList.equals(koron.otherList) : koron.otherList != null)
            && !(sameList != null ? !sameList.equals(koron.sameList) : koron.sameList != null)
            && !(someList != null ? !someList.equals(koron.someList) : koron.someList != null);
  }

  @Override
  public int hashCode() {
    int result = someList != null ? someList.hashCode() : 0;
    result = 31 * result + (sameList != null ? sameList.hashCode() : 0);
    result = 31 * result + (otherList != null ? otherList.hashCode() : 0);
    return result;
  }
}
