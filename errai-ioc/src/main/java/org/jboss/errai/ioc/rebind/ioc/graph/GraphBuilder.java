package org.jboss.errai.ioc.rebind.ioc.graph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.codegen.meta.MetaClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds a dependency graph for use by the container.
 *
 * @author Mike Brock
 */
public class GraphBuilder {
  private final Map<String, MetaClass> classLookup = new HashMap<String, MetaClass>(100);
  private final Multimap<String, Dependency> dependencyMap = HashMultimap.create();
  private final Multimap<String, Object> itemMap = HashMultimap.create();

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

  /**
   * Returns the number of recorded dependencies for the specified type.
   *
   * @param type the type to query for the number of dependencies.
   * @return the number of dependencies for the specified type.
   */
  public int getDependencyCount(final MetaClass type) {
    return dependencyMap.get(type.getFullyQualifiedName()).size();
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

    for (final String type : itemMap.keySet()) {
      sortUnitList.add(_build(sortUnitHashMap, type));
    }

    return sortUnitList;
  }

  private SortUnit _build(final Map<String, SortUnit> sortUnits, final String type) {
    if (sortUnits.containsKey(type)) {
      return sortUnits.get(type);
    }

    final ProxySortUnit proxySortUnit = ProxySortUnit.proxyOf(classLookup.get(type));
    sortUnits.put(type, proxySortUnit);

    final Collection<Object> items = itemMap.get(type);
    final Collection<Dependency> deps = dependencyMap.get(type);

    final List<SortUnit> sortUnitDependencies = new ArrayList<SortUnit>();
    for (final Dependency d : deps) {
      sortUnitDependencies.add(_build(sortUnits, d.getType().getFullyQualifiedName()));
    }

    proxySortUnit.setDelegate(SortUnit.create(classLookup.get(type), items, sortUnitDependencies));

    return proxySortUnit;
  }

  public String toDOTRepresentation() {
    return toDOTRepresentation(build());
  }

  public static String toDOTRepresentation(final List<SortUnit> graph) {
    final StringBuilder sb = new StringBuilder("digraph g {\n");
    final Set<String> visited = new HashSet<String>();
    for (final SortUnit su : graph) {
      if (!visit(visited, su)) {
        continue;
      }

      if (su.getDependencies().isEmpty()) {
        sb.append("  ").append(quote(su)).append("\n");
      }
      else {
        for (final SortUnit node : su.getDependencies()) {
          if (!visit(visited, su, node)) {
            continue;
          }

          sb.append("  ").append(quote(su)).append(" -> ").append(quote(node)).append("\n");
        }
      }
    }
    return sb.append("}").toString();
  }

  private static boolean visit(final Set<String> visited, final SortUnit su) {
    if (visited.contains(su.getType().getFullyQualifiedName())) {
      return false;
    }
    else {
      visited.add(su.getType().getFullyQualifiedName());
      return true;
    }
  }

  private static boolean visit(final Set<String> visited, final SortUnit from, final SortUnit to) {
    final String str = from.getType().getFullyQualifiedName() + "->" + to.getType().getFullyQualifiedName();
    if (visited.contains(str)) {
      return false;
    }
    else {
      visited.add(str);
      return true;
    }
  }

  public boolean hasType(MetaClass type) {
    return dependencyMap.containsKey(type.getFullyQualifiedName());
  }

  /**
   * Returns a quoted representation of the object's toString() suitable for use
   * in the GraphViz DOT language.
   *
   * @param n The object whose quoted string representation to generate.
   * @return A quoted string representation of n.
   */
  private static String quote(final SortUnit n) {
    // note that only " is escaped in the DOT language (a bare backslash is just a bare backslash)
    return "\"" + n.getType().getFullyQualifiedName().replaceAll("\\Q\"\\E", "\\\"") + "\"";
  }
}
