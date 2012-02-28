package org.jboss.errai.common.client.graph;

/**
 * Utility for printing out graphs so they are easier to reason about.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class GraphPrinter {

  /**
   * Returns a DOT language representation of the given directed graph.
   *
   * @param g
   *          The graph to render as a DOT file.
   * @return A string that can be fed into the GraphViz program in order to
   *         produce a visual representation of g.
   */
  public static <V> String toDotFile(Digraph<V> g) {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph g {\n");
    for (V node : g.getNodes()) {
      sb.append("  ").append(quote(node)).append("\n");
      for (V toNode : g.nodesReferencedFrom(node)) {
        sb.append("  ").append(quote(node)).append(" -> ").append(quote(toNode)).append(" [ color = RED ]\n");
      }
      for (V toNode : g.nodesReferencing(node)) {
        sb.append("  ").append(quote(toNode)).append(" -> ").append(quote(node)).append(" [ color = BLUE ]\n");
      }
    }
    sb.append("}\n");
    return sb.toString();
  }

  /**
   * Returns a quoted representation of the object's toString() suitable for use
   * in the GraphViz DOT language.
   *
   * @param n The object whose quoted string representation to generate.
   * @return A quoted string representation of n.
   */
  private static String quote(Object n) {
    // note that only " is escaped in the DOT language (a bare backslash is just a bare backslash)
    return "\"" + n.toString().replaceAll("\\Q\"\\E", "\\\"") + "\"";
  }
}
