package org.jboss.errai.codegen.framework.builder;

/**
 * @author Mike Brock
 */
public interface MethodCommentBuilder<T> extends MethodBlockBuilder<T> {
  public MethodBlockBuilder<T> methodComment(String comment);
}
