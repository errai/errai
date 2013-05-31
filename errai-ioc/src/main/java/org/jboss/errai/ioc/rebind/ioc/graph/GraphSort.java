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

package org.jboss.errai.ioc.rebind.ioc.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Topological sort algorithm to sort the dependency graph prior to emission of the generated code for the IOC
 * bootstrapper.
 *
 * @author Mike Brock
 */
public final class GraphSort {
  private GraphSort() {
  }

  /**
   * Performs of a topological sort and returns a new list of sorted {@link SortUnit}s.
   *
   * @param in
   *         a list of sort units to be sorted.
   *
   * @return a new sorted lis
   */
  public static List<SortUnit> sortGraph(final Collection<SortUnit> in) {
    final List<SortUnit> sortUnitList = topologicalSort(new ArrayList<SortUnit>(in));
    Collections.sort(sortUnitList);
    return sortUnitList;
  }

  private static List<SortUnit> topologicalSort(final List<SortUnit> toSort) {
    final Set<String> visited = new HashSet<String>();
    final List<SortUnit> sorted = new ArrayList<SortUnit>();
    for (final SortUnit n : toSort) {
      _topologicalSort(visited, sorted, n);
    }
    return sorted;
  }

  private static void _topologicalSort(final Set<String> visited,
                                       final List<SortUnit> sorted,
                                       final SortUnit n) {

    if (!visited.contains(n.getType().getFullyQualifiedName())) {
      visited.add(n.getType().getFullyQualifiedName());
      for (final SortUnit m : n.getDependencies()) {
        _topologicalSort(visited, sorted, m);
      }
      sorted.add(n);
    }
  }
}
