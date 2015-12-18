/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.rebind;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.jboss.errai.common.client.api.Assert;

import antlr.collections.AST;

/**
 * Facilitates iteration through the nodes of an ANTLR AST. The iteration order
 * is an in-order traversal of the nodes in the tree.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class AstInorderTraversal implements Iterator<AST> {

  private final AST root;
  private final Deque<AST> context = new ArrayDeque<AST>();
  private AST nextNode;

  /**
   * Creates a new traversal of the given AST. Multiple traversers can exist
   * independently on the same AST.
   *
   * @param ast
   *          The AST to traverse. Must not be null.
   */
  public AstInorderTraversal(AST ast) {
    this.root = Assert.notNull(ast);
    nextNode = root;
  }

  @Override
  public boolean hasNext() {
    return nextNode != null;
  }

  @Override
  public AST next() {
    if (!hasNext())
      throw new IllegalStateException("This traversal is done");

    final AST thisNode = nextNode;

    // look forward for the next next node
    if (nextNode.getFirstChild() != null) {
      context.push(nextNode);
      nextNode = nextNode.getFirstChild();
    }
    else if (nextNode.getNextSibling() != null) {
      nextNode = nextNode.getNextSibling();
    }
    else {
      nextNode = null;
      while (!context.isEmpty() && nextNode == null) {
        AST beenThere = context.pop();
        if (beenThere.getNextSibling() != null) {
          nextNode = beenThere.getNextSibling();
          break;
        }
      }
    }

    return thisNode;
  }

  /**
   * Returns the stack of AST nodes that lead from the root of the AST down to
   * the current node that this traverser is positioned on.
   *
   * @return A sequence of nodes, where the first item in the list is the root
   *         of the AST, the last item is the current node, and all elements
   *         have the relationship that the node at position {@code p} is the
   *         parent of the node at position {@code p + 1}.
   */
  public Deque<AST> context() {
    return context;
  }

  /**
   * Advances this iterator past the subtree rooted at the given node. The
   * cursor will be left in one of the following places, depending on the
   * position of the node:
   * <ol>
   *   <li>On the sibling node immediately following the given node, if such a
   *   node exists
   *   <li>On an "uncle": the next sibling of the first ancestor node that has an
   *   unvisited sibling, if such a node exists
   *   <li>At the end of the iteration ({@link #hasNext()} will return false).
   * </ol>
   *
   * @param node
   *          The node to fast-forward past.
   */
  public void fastForwardToNextSiblingOf(AST node) {
    // phase 1: search for the requested node in the context stack
    for (;;) {
      if (context.isEmpty()) {
        throw new IllegalArgumentException("The given node " + node
                + " was not a parent node of the starting point");
      }
      if (context.pop() == node) {
        context.push(node);
        break;
      }
    }

    // phase 2: find the next node after the requested one, which might be a
    // direct sibling or a sibling of an ancestor
    nextNode = null;
    while (!context.isEmpty() && nextNode == null) {
      AST beenThere = context.pop();
      if (beenThere.getNextSibling() != null) {
        nextNode = beenThere.getNextSibling();
        break;
      }
    }
  }

  public AST fastForwardTo(int nodeType) {
    while (hasNext()) {
      AST ast = next();
      if (ast.getType() == nodeType) {
        return ast;
      }
    }
    return null;
  }

  public AST fastForwardTo(AST node) {
    while (hasNext()) {
      AST ast = next();
      if (ast == node) {
        return ast;
      }
    }
    throw new IllegalArgumentException("Didn't find requested node in the remainder of the traversal");
  }

  /**
   * Not implemented.
   *
   * @throws UnsupportedOperationException
   *           when called
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Not implemented");
  }
}
