package org.jboss.errai.common.client.graph;

import java.util.Set;

/**
 * Represents a directed graph of nodes.
 *
 * @param V The vertex (node) type
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface Digraph<V> {

  /**
   * Returns all the nodes in this graph.
   *
   * @return The set of nodes in this graph. Never null.
   */
  Set<V> getNodes();

  /**
   * Returns the set of nodes that are reachable from the given node by
   * traversing exactly one outbound edge. If you were looking at a drawing of
   * this graph on paper, this method would return the set of nodes that are
   * pointed to by edges eminating from {@code fromNode}.
   *
   * @param fromNode
   *          the node in question. Behaviour is undefined if this node is not
   *          part of this graph.
   * @return The directly reachable neighbours of {@code fromNode}.
   */
  Set<V> nodesReferencedFrom(V fromNode);

  /**
   * Returns the set of nodes that are can reach the given node by traversing
   * exactly one of their own outbound edges. If you were looking at a drawing
   * of this graph on paper, this method would return the set of nodes that are
   * pointing at {@code fromNode}.
   *
   * @param fromNode
   *          the node in question. Behaviour is undefined if this node is not
   *          part of this graph.
   * @return The neighbours of {@code fromNode} that point at it.
   */
  Set<V> nodesReferencing(V toNode);
}
