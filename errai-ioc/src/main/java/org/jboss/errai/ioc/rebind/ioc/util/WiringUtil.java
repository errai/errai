/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.rebind.ioc.util;

import org.jboss.errai.ioc.rebind.SortUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Mike Brock
 */
public class WiringUtil {
  public static List<SortUnit> worstSortAlgorithmEver(Collection<SortUnit> in) {
    List<SortUnit> newList = new ArrayList<SortUnit>(in);

    _worstSort(newList);
    _worstSort(newList);

    return newList;
  }
  
  private static void _worstSort(List<SortUnit> newList) {
    for (int i = 0; i < newList.size(); i++) {
      SortUnit s = newList.get(i);
      for (int y = i; y < newList.size(); y++) {
        SortUnit c = newList.get(y);
        
        if (s == c) {
          continue;
        }
        if (s.getDependencies().contains(c)) {
          newList.add(i, newList.remove(y));
        }
      }
    }
  }
}
