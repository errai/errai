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
 * Builds a dependency graph for use by the container.
 *
 * @author Mike Brock
 */
public class GraphBuilder {
  public final Map<String, MetaClass> classLookup = new HashMap<String, MetaClass>();
  public final Multimap<String, Dependency> dependencyMap = HashMultimap.create();
  public final Multimap<String, Object> itemMap = HashMultimap.create();

  /**
   * Records a dependency on the specified type.
   *
   * @param type       the type to record a dependency on.
   * @param dependency the depedency
   * @return the same instance of the GraphBuilder that called this method.
   */
  public GraphBuilder addDependency(final MetaClass type, final Dependency dependency) {
    dependencyMap.put(type.getFullyQualifiedName(), dependency);
    recordClassForLookup(dependency.getType());
    return this;
  }

  /**
   * Record an arbitrary object to be associated with a type.
   *
   * @param type the type to record the item for.
   * @param item the arbitrary object
   * @return the same instance of the GraphBuilder that called this method.
   */
  public GraphBuilder addItem(final MetaClass type, final Object item) {
    itemMap.put(type.getFullyQualifiedName(), item);
    recordClassForLookup(type);
    return this;
  }

  private void recordClassForLookup(final MetaClass type) {
    classLookup.put(type.getFullyQualifiedName(), type);
  }

  /**
   * Returns a graph of only incoming edges for use in a topological sort.
   *
   * @return a list of incoming edges in the graph.
   */
  public List<SortUnit> build() {
    final List<SortUnit> sortUnitList = new ArrayList<SortUnit>(10);
    final HashMap<String, SortUnit> sortUnitHashMap = new HashMap<String, SortUnit>(10);

    for (String type : itemMap.keySet()) {
      sortUnitList.add(_build(sortUnitHashMap, type));
    }

    return sortUnitList;
  }

  private SortUnit _build(Map<String, SortUnit> sortUnits, String type) {
    if (sortUnits.containsKey(type)) {
      return sortUnits.get(type);
    }

    final ProxySortUnit proxySortUnit = ProxySortUnit.proxyOf(classLookup.get(type));
    sortUnits.put(type, proxySortUnit);

    final Collection<Object> items = itemMap.get(type);
    final Collection<Dependency> deps = dependencyMap.get(type);

    List<SortUnit> sortUnitDependencies = new ArrayList<SortUnit>();
    for (Dependency d : deps) {
      sortUnitDependencies.add(_build(sortUnits, d.getType().getFullyQualifiedName()));
    }

    proxySortUnit.setDelegate(SortUnit.create(classLookup.get(type), items, sortUnitDependencies));

    return proxySortUnit;
  }
}
