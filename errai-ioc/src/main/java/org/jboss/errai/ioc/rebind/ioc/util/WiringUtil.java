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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class WiringUtil {
  public static List<SortUnit> worstSortAlgorithmEver(final Collection<SortUnit> in) {
    List<SortUnit> newList = new ArrayList<SortUnit>(in);
    // perform fist pass.
    _worstSort(newList);

    return newList;
  }

  private static void _worstSort(final List<SortUnit> newList) {
    int noSortCount = 0;
    for (int i = 0; i < newList.size(); ) {
      SortUnit s = newList.get(i);
      boolean adv = true;
      for (int y = i + 1; y < newList.size(); y++) {
        SortUnit c = newList.get(y);

        if (s != c && s.getDependencies().contains(c)) {
           newList.add(i, newList.remove(y));
          adv = false;
          
          for (SortUnit chk : s.getDependencies()) {
            if (chk.equals(c) && chk.isHard()) {
              adv = true;
            }
          }
        }
      }
      if (adv || noSortCount > 1) {
        i++;
        noSortCount = 0;
      }
      else {
        noSortCount++;
      }
    }
  }
}
