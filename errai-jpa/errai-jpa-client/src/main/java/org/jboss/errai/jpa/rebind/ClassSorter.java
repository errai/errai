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

package org.jboss.errai.jpa.rebind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.ManagedType;

public class ClassSorter {

  private enum Colour { GREY, BLACK };

  /**
   * Sorts the given list of JPA ManagedType objects so that supertypes come before their subtypes in the list.
   *
   * @param types the list of metatypes to sort. This list will not be modified.
   * @return A new list with the same elements as those in the given list, but possibly in a different order.
   */
  public static List<ManagedType<?>> supertypesFirst(Collection<? extends ManagedType<?>> types) {
    List<ManagedType<?>> sorted = new ArrayList<ManagedType<?>>(types.size());
    Map<ManagedType<?>, Colour> visited = new IdentityHashMap<ManagedType<?>, ClassSorter.Colour>();

    Map<Class<?>, ManagedType<?>> allManagedTypes = new HashMap<Class<?>, ManagedType<?>>();
    for (ManagedType<?> type : types) {
      allManagedTypes.put(type.getJavaType(), type);
    }

    for (ManagedType<?> type : types) {
      Colour c = visited.get(type);
      if (c == null) {
        visit(type, sorted, visited, allManagedTypes);
      }
    }
    return sorted;
  }

  /**
   * Recursive subroutine of {@link #supertypesFirst(List)}. Implements the
   * topological sort algorithm described on Wikipedia as of October 2, 2013.
   *
   * @param node
   *          The node to visit.
   * @param sorted
   *          The output list of sorted nodes (gets appended to during call).
   * @param visited
   *          The map expressing "temporary" (grey) and "permanent" (black)
   *          marking status of the nodes.
   * @param all
   *          A lookup table that maps Java types to the ManagedType instances
   *          that represent them.
   */
  private static void visit(ManagedType<?> node, List<ManagedType<?>> sorted,
          Map<ManagedType<?>, Colour> visited, Map<Class<?>, ManagedType<?>> all) {
    Colour c = visited.get(node);
    if (c == Colour.GREY) {
      throw new IllegalArgumentException("Type graph is cyclic! Cycle found at " + node.getJavaType());
    }
    if (c == null) {
      visited.put(node, Colour.GREY);
      Class<?> superclass = node.getJavaType().getSuperclass();
      ManagedType<?> superManagedType = all.get(superclass);
      if (superManagedType != null) {
        visit(superManagedType, sorted, visited, all);
      }
      visited.put(node, Colour.BLACK);
      sorted.add(node);
    }
  }
}
