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

import com.google.gwt.dev.util.collect.IdentityHashSet;
import org.jboss.errai.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class SortUnit implements Comparable<SortUnit>, GraphNode {

  private final MetaClass type;
  private final List<Object> items;
  private final Set<SortUnit> dependencies;
  private final boolean hard;

  protected SortUnit(MetaClass type, List<Object> items, Set<SortUnit> dependencies, boolean hard) {
    this.type = type.getErased();
    this.items = Collections.unmodifiableList(items);
    this.dependencies = Collections.unmodifiableSet(dependencies);
    this.hard = hard;
  }

  public static SortUnit create(MetaClass type, Collection<Object> items, Collection<SortUnit> dependencies, boolean hard) {
    return new SortUnit(type, new ArrayList<Object>(items), new HashSet<SortUnit>(dependencies), hard);
  }

  public static SortUnit copyOfAsHard(SortUnit toCopy) {
    return new SortUnit(toCopy.getType(), toCopy.getItems(), toCopy.getDependencies(), true);
  }

  public MetaClass getType() {
    return type;
  }

  public List<Object> getItems() {
    return items;
  }

  public Set<SortUnit> getDependencies() {
    return dependencies;
  }

  public SortUnit getDependency(SortUnit unit) {
    for (SortUnit s : getDependencies()) {
      if (s.equals(unit)) return s;
    }
    return null;
  }

  public boolean isHard() {
    return hard;
  }

  public int getDepth() {
    int depth = 0;
    for (SortUnit su : getDependencies()) {
      if (su.equals(this)) continue;

      int d = _getDepth(new IdentityHashSet<SortUnit>(), this, 1, su);
      if (d > depth) {
        depth = d;
      }
    }
    return depth;
  }


  private static int _getDepth(Set<SortUnit> visited, SortUnit outer, int depth, SortUnit su) {
    if (visited.contains(su)) {
      return 0;
    }

    visited.add(su);

    for (SortUnit dep : su.getDependencies()) {
      if (dep.equals(outer)) continue;

      int d = _getDepth(visited, outer, depth + 1, dep);
      if (d > depth) {
        depth = d;
      }
    }
    return depth;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SortUnit)) return false;

    SortUnit sortUnit = (SortUnit) o;

    return type != null && type.getFullyQualifiedName().equals(sortUnit.type.getFullyQualifiedName());
  }

  @Override
  public int compareTo(SortUnit o) {
    if (o.getDependencies().contains(this) && getDependencies().contains(o)) {
      if (o.getDependencies().contains(this) && o.getDependency(this).isHard()) {
        return 0;
      }
      else if (getDependencies().contains(o) && getDependency(o).isHard()) {
        return 0;
      }

      return getDepth() - o.getDepth();
    }
    else {
      return 0;
    }
  }

  @Override
  public int hashCode() {
    return type != null ? type.getFullyQualifiedName().hashCode() * 37 : 0;
  }

  @Override
  public String toString() {
    return _toString(new HashSet<SortUnit>());
  }

  private String _toString(Set<SortUnit> visited) {
    return "(depth:" + getDepth() + ";hard=" + hard + ")" + type.toString()
            + " => " + _renderDependencyTree(visited, this);
  }


  private static String _renderDependencyTree(Set<SortUnit> visited, SortUnit visit) {
    if (visited.contains(visit)) {
      return "<CYCLE>";
    }
    visited.add(visit);

    StringBuilder sb = new StringBuilder("[");
    Iterator<SortUnit> iter = visit.dependencies.iterator();
    while (iter.hasNext()) {
      sb.append(iter.next()._toString(visited));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.append("]").toString();
  }
}
