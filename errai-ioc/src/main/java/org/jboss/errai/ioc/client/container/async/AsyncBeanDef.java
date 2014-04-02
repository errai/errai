package org.jboss.errai.ioc.client.container.async;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.api.ActivatedBy;
import org.jboss.errai.ioc.client.container.BeanActivator;

/**
 * @author Mike Brock
 */
public interface AsyncBeanDef<T> {
  /**
   * Returns the type of the bean.
   *
   * @return the type of the bean.
   *
   * @see #getBeanClass()
   */
  public Class<T> getType();

  /**
   * Returns the actual bean class represented by this bean.
   *
   * @return the actual type of the bean.
   */
  public Class<?> getBeanClass();

  /**
   * Returns the scope of the bean.
   *
   * @returns the annotation type representing the scope of the bean.
   */
  public Class<? extends Annotation> getScope();

  /**
   * Returns an instance of the bean within the active scope.
   *
   * @return The bean instance.
   */
  public void getInstance(CreationalCallback<T> callback);

  /**
   * Returns an instance of the bean within the active scope, using the specified SimpleCreationalContext.
   *
   * @param context
   *
   * @return
   */
  void getInstance(CreationalCallback<T> callback, AsyncCreationalContext context);

  /**
   * Returns a new instance of the bean. Calling this method overrides the underlying scope and instantiates a new
   * instance of the bean.
   *
   * @return a new instance of the bean.
   */
  public void newInstance(CreationalCallback<T> callback);

  /**
   * Returns any qualifiers associated with the bean.
   *
   * @return
   */
  public Set<Annotation> getQualifiers();

  /**
   * Returns true if the beans qualifiers match the specified set of qualifiers.
   *
   * @param annotations
   *     the qualifiers to compare
   *
   * @return returns whether or not the bean matches the set of qualifiers
   */
  public boolean matches(Set<Annotation> annotations);

  /**
   * Returns the name of the bean.
   *
   * @return the name of the bean. If the bean does not have a name, returns null.
   */
  public String getName();

  /**
   * Returns true if the bean is a concrete bean definition and not an interface or abstract type.
   *
   * @return true if concrete.
   */
  public boolean isConcrete();
  
  /**
   * Returns true if the bean is activated. All managed beans are activated by default unless a
   * {@link BeanActivator} was specified using {@link ActivatedBy} which will be consulted when
   * invoking this method.
   * 
   * @return true if activated, otherwise false.
   */
  public boolean isActivated();
}
