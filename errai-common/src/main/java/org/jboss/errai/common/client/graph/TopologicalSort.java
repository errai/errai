package org.jboss.errai.common.client.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implements the <i>topological sort</i> algorithm for directed graphs.
 * <p>
 * Algorithm taken from Wikipedia: http://en.wikipedia.org/wiki/Topological_sorting
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TopologicalSort {

  public static <V> List<V> topologicalSort(Digraph<V> g) {
//  L <- Empty list that will contain the sorted nodes
    List<V> sorted = new ArrayList<V>(g.getNodes().size());

//  S <- Set of all nodes with no outgoing edges
    Set<V> s = findNodesWithNoOutgoingEdges(g);

//  for each node n in S do
    Set<V> visited = new HashSet<V>();
    for (V n : s) {
//    visit(n)
      visit(g, n, visited, sorted);
    }

    return sorted;
  }

//function visit(node n)
  private static <V> void visit(Digraph<V> g, V n, Set<V> visited, List<V> sorted) {
//  if n has not been visited yet then
    if (!visited.contains(n)) {
//    mark n as visited
      visited.add(n);
//    for each node m with an edge from m to n do
      for (V m : g.nodesReferencing(n)) {
//      visit(m)
        visit(g, m, visited, sorted);
      }
//    add n to L
      sorted.add(n);
    }
  }

  public static <V> Set<V> findNodesWithNoIncomingEdges(Digraph<V> g) {
    Set<V> s = new HashSet<V>(g.getNodes());
    for (V node : g.getNodes()) {
      s.removeAll(g.nodesReferencing(node));
    }
    return s;
  }

  public static <V> Set<V> findNodesWithNoOutgoingEdges(Digraph<V> g) {
    Set<V> s = new HashSet<V>();
    for (V node : g.getNodes()) {
      if (g.nodesReferencing(node).isEmpty()) {
        s.add(node);
      }
    }
    return s;
  }
}
