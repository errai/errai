package org.jboss.errai.common.client.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TopologicalSortTest {

  private static class Node {
    private final String name;
    private final Set<Node> pointsTo = new HashSet<Node>();

    public Node(String name) {
      this.name = name;
    }

    public void addEdgeTo(Node pointTo) {
      pointsTo.add(pointTo);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static class MutableDigraph implements Digraph<Node> {
    private final Set<Node> nodeSet = new HashSet<Node>();

    @Override
    public Set<Node> getNodes() {
      return nodeSet;
    }

    public void addNode(Node n) {
      nodeSet.add(n);
    }

    @Override
    public Set<Node> nodesReferencedFrom(Node fromNode) {
      return fromNode.pointsTo;
    }

    @Override
    public Set<Node> nodesReferencing(Node toNode) {
      Set<Node> referencing = new HashSet<Node>();
      for (Node n : nodeSet) {
        if (n.pointsTo.contains(toNode)) {
          referencing.add(n);
        }
      }
      return referencing;
    }
  }

  /**
   * Tests a graph that has only one valid sorted order.
   */
  @Test
  public void testSimpleSort() {
    MutableDigraph g = new MutableDigraph();
    Node n1 = new Node("n1");
    Node n2 = new Node("n2");
    Node n3 = new Node("n3");

    g.addNode(n1);
    g.addNode(n2);
    g.addNode(n3);

    n1.addEdgeTo(n2);
    n2.addEdgeTo(n3);

    List<Node> sorted = TopologicalSort.topologicalSort(g);
    Assert.assertEquals(Arrays.asList(n1, n2, n3), sorted);
  }

  /**
   * Tests a graph that has one possible start node, one possible end node, and
   * a "fan out" in the middle that could be sorted in arbitrary order.
   */
  @Test
  public void testGraphWithMultipleSortOrders() {
    MutableDigraph g = new MutableDigraph();
    Node n1 = new Node("n1");
    Node n2_1 = new Node("n2.1");
    Node n2_2 = new Node("n2.2");
    Node n2_3 = new Node("n2.3");
    Node n2_4 = new Node("n2.4");
    Node n3 = new Node("n3");

    g.addNode(n1);
    g.addNode(n2_1);
    g.addNode(n2_2);
    g.addNode(n2_3);
    g.addNode(n2_4);
    g.addNode(n3);

    n1.addEdgeTo(n2_1);
    n1.addEdgeTo(n2_2);
    n1.addEdgeTo(n2_3);
    n1.addEdgeTo(n2_4);
    n2_1.addEdgeTo(n3);
    n2_2.addEdgeTo(n3);
    n2_3.addEdgeTo(n3);
    n2_4.addEdgeTo(n3);

    List<Node> sorted = TopologicalSort.topologicalSort(g);

    // check first and last
    Assert.assertEquals(6, sorted.size());
    Assert.assertEquals(n1, sorted.get(0));
    Assert.assertEquals(n3, sorted.get(5));

    // check middle contains all the n2_? nodes (arbitrary order)
    Set<Node> n2fanoutExpected = new HashSet<Node>(Arrays.asList(n2_1, n2_2, n2_3, n2_4));
    Set<Node> n2fanoutActual = new HashSet<Node>(sorted.subList(1, 5));
    Assert.assertEquals(n2fanoutExpected, n2fanoutActual);
  }
}
