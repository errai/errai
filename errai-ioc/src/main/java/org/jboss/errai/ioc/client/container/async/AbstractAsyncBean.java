package org.jboss.errai.ioc.client.container.async;

import org.jboss.errai.ioc.client.QualifierUtil;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Mike Brock
 */
public abstract class AbstractAsyncBean<T> implements AsyncBeanDef<T> {
  protected String name;
  protected Class<T> type;
  protected Class<?> beanType;
  protected Set<Annotation> qualifiers;
  protected boolean concrete;

  /**
   * Returns the absolute type of the bean
   *
   * @return a class representing the absolute type of the bean
   */
  public Class<T> getType() {
    return type;
  }

  @Override
  public Class<?> getBeanClass() {
    return beanType;
  }

  @Override
  public String getName() {
    return name;
  }

  public boolean isConcrete() {
    return concrete;
  }

  /**
   * Returns a set of qualifiers associated with this bean
   *
   * @return A set of qualifiers. Returns an empty set if none.
   */
  public Set<Annotation> getQualifiers() {
    return qualifiers;
  }

  /**
   * Returns true if the underlying bean contains all of the annotations specified.
   *
   * @param annotations
   *     a set of annotations to compare
   *
   * @return true if matches
   */
  public boolean matches(final Set<Annotation> annotations) {
    return QualifierUtil.matches(annotations, qualifiers);
  }
}
