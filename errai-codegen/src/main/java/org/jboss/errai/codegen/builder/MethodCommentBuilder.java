package org.jboss.errai.codegen.builder;

/**
 * @author Mike Brock
 */
public interface MethodCommentBuilder<T> extends MethodBlockBuilder<T> {
  public MethodBlockBuilder<T> methodComment(String comment);
}
