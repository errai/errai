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

import org.jboss.errai.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

  public static Set<List<SortUnit>> sortAndPartitionGraph(final Collection<SortUnit> in) {
    final Map<MetaClass, Set<SortUnit>> builderMap = new LinkedHashMap<MetaClass, Set<SortUnit>>();

    for (final SortUnit unit : sortGraph(in)) {
      final Set<SortUnit> traversal = new HashSet<SortUnit>();
      _traverseGraphExtent(traversal, unit);

      final Set<SortUnit> partition = new HashSet<SortUnit>(traversal);
      for (final SortUnit tUnit : traversal) {
        final Set<SortUnit> c = builderMap.get(tUnit.getType());
        if (c != null) {
          partition.addAll(c);
        }
      }

      for (final SortUnit partitionedUnit : partition) {
        builderMap.put(partitionedUnit.getType(), partition);
      }
    }

    final Set<List<SortUnit>> consolidated = new LinkedHashSet<List<SortUnit>>();
    final Map<Set<SortUnit>, List<SortUnit>> sortingCache = new IdentityHashMap<Set<SortUnit>, List<SortUnit>>();


    for (final Map.Entry<MetaClass, Set<SortUnit>> metaClassSetEntry : builderMap.entrySet()) {
      if (!sortingCache.containsKey(metaClassSetEntry.getValue())) {
        sortingCache.put(metaClassSetEntry.getValue(), sortGraph(metaClassSetEntry.getValue()));
      }

      consolidated.add(sortingCache.get(metaClassSetEntry.getValue()));
    }

    return consolidated;
  }

  private static void _traverseGraphExtent(final Set<SortUnit> partition,
                                           final SortUnit toVisit) {
    if (partition.contains(toVisit)) return;
    partition.add(toVisit);

    for (final SortUnit dep : toVisit.getDependencies()) {
      _traverseGraphExtent(partition, dep);
    }
  }


  private static List<SortUnit> topologicalSort(List<SortUnit> toSort) {
    final Set<String> visited = new HashSet<String>();
    final List<SortUnit> sorted = new ArrayList<SortUnit>();
    for (SortUnit n : toSort) {
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
