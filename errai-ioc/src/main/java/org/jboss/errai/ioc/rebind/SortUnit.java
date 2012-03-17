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

package org.jboss.errai.ioc.rebind;

import org.jboss.errai.codegen.framework.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class SortUnit implements Comparable<SortUnit> {
  private final MetaClass type;
  private final List<Object> items;
  private final Set<SortUnit> dependencies;
  private final boolean hard;

  public SortUnit(MetaClass type) {
    this.type = type;
    this.dependencies = Collections.emptySet();
    this.items = new ArrayList<Object>();
    this.hard = false;
  }

  public SortUnit(MetaClass type, boolean hard) {
    this.type = type;
    this.dependencies = Collections.emptySet();
    this.items = new ArrayList<Object>();
    this.hard = hard;
  }

  public SortUnit(MetaClass type, Object item) {
    this(type, item, Collections.<SortUnit>emptySet());
  }

  public SortUnit(MetaClass type, Object item, Set<SortUnit> dependencies) {
    this.type = type;
    this.items = new ArrayList<Object>();
    if (item != null) {
      this.items.add(item);
    }
    this.dependencies = dependencies;
    this.hard = false;
  }

  public SortUnit(MetaClass type, List<Object> items, Set<SortUnit> dependencies, boolean hard) {
    this.type = type;
    this.items = items;
    this.dependencies = dependencies;
    this.hard = hard;
  }

  public void addItem(Object item) {
    items.add(item);
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

  public boolean isHard() {
    return hard;
  }

  public int getDepth() {
    int depth = 0;
    for (SortUnit su : getDependencies()) {
      if (su.equals(this)) continue;

      int d = _getDepth(this, 1, su);
      if (d > depth) {
        depth = d;
      }
    }
    return depth;
  }

  private static int _getDepth(SortUnit outer, int depth, SortUnit su) {
    for (SortUnit dep : su.getDependencies()) {
      if (dep.equals(outer)) continue;

      int d = _getDepth(outer, depth + 1, dep);
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

    return !(type != null ? !type.equals(sortUnit.type) : sortUnit.type != null);

  }

  @Override
  public int compareTo(SortUnit o) {
    if (o.getDependencies().contains(this) || getDependencies().contains(o)) {
      return 0;
    }
    else if (o.getDepth() < getDepth()) {
      return 1;
    }
    else {
      return 0;
    }
  }

  @Override
  public int hashCode() {
    return type != null ? type.hashCode() : 0;
  }

  @Override
  public String toString() {
    return " (depth:" + getDepth() + ")" + type.toString() + " => " + dependencies;
  }
}
