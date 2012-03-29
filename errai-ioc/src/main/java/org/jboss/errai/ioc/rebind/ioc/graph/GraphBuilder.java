package org.jboss.errai.ioc.rebind.ioc.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds graphs
 *
 * @author Mike Brock
 */
public class GraphBuilder {
  public final Map<String, MetaClass> classLookup = new HashMap<String, MetaClass>();
  public final Multimap<String, Dependency> dependencyMap = HashMultimap.create();
  public final Multimap<String, Object> itemMap = HashMultimap.create();

  public GraphBuilder addDependency(MetaClass type, Dependency dependency) {
    dependencyMap.put(type.getFullyQualifiedName(), dependency);
    recordClassForLookup(dependency.getType());
    return this;
  }

  public GraphBuilder addItem(MetaClass type, Object item) {
    itemMap.put(type.getFullyQualifiedName(), item);
    recordClassForLookup(type);
    return this;
  }

  private void recordClassForLookup(MetaClass type) {
    classLookup.put(type.getFullyQualifiedName(), type);
  }

  public List<SortUnit> build() {
    final List<SortUnit> sortUnitList = new ArrayList<SortUnit>(10);
    final HashMap<String, SortUnit> sortUnitHashMap = new HashMap<String, SortUnit>(10);

    for (String type : itemMap.keySet()) {
      sortUnitList.add(_build(sortUnitHashMap, type, false));
    }

    return sortUnitList;
  }

  private SortUnit _build(Map<String, SortUnit> sortUnits, String type, boolean hard) {
    if (sortUnits.containsKey(type)) {
      if (hard) {
        return ProxySortUnit.hardDepProxy(classLookup.get(type), sortUnits.get(type));
      }
      else {
        return sortUnits.get(type);
      }
    }

    final ProxySortUnit proxySortUnit = ProxySortUnit.proxyOf(classLookup.get(type));
    sortUnits.put(type, proxySortUnit);

    final Collection<Object> items = itemMap.get(type);
    final Collection<Dependency> deps = dependencyMap.get(type);

    List<SortUnit> sortUnitDependencies = new ArrayList<SortUnit>();
    for (Dependency d : deps) {
      sortUnitDependencies.add(_build(sortUnits, d.getType().getFullyQualifiedName(), d.isHard()));
    }

    proxySortUnit.setDelegate(SortUnit.create(classLookup.get(type), items, sortUnitDependencies, false));

    return proxySortUnit;
  }
}
