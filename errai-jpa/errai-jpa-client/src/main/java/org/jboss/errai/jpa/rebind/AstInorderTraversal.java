package org.jboss.errai.jpa.rebind;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jboss.errai.common.client.framework.Assert;

import antlr.collections.AST;

public class AstInorderTraversal {

  private final AST root;
  private final Deque<AST> context = new ArrayDeque<AST>();
  private AST nextNode;

  public AstInorderTraversal(AST ast) {
    this.root = Assert.notNull(ast);
    nextNode = root;
  }

  public boolean hasNext() {
    return nextNode != null;
  }

  public AST next() {
    if (!hasNext()) throw new IllegalStateException("This traversal is done");

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

  public Deque<AST> context() {
    return context;
  }

  public void fastForwardToNextSiblingOf(AST node) {
    // phase 1: search for the requested node in the context stack
    for (;;) {
      if (context.isEmpty()) {
        throw new IllegalArgumentException("The given node " + node + " was not a parent node of the starting point");
      }
      if (context.pop() == node) {
        context.push(node);
        break;
      }
    }

    // phase 2: find the next node after the requested one, which might be a direct sibling or a sibling of an ancestor
    nextNode = null;
    while (!context.isEmpty() && nextNode == null) {
      AST beenThere = context.pop();
      if (beenThere.getNextSibling() != null) {
        nextNode = beenThere.getNextSibling();
        break;
      }
    }
  }
}
