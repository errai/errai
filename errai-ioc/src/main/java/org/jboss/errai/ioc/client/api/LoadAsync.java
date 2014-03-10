package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.google.gwt.core.client.GWT;

/**
 * Provides a hint to the container that the annotated element should be loaded asynchronously using
 * GWT's code splitting. This annotation can be placed on any container-managed element (beans or
 * producers).
 * 
 * A fragment name can optionally be specified using a class literal. GWT's code splitter will put
 * the code of all types with the same fragment name into the same code fragment see
 * {@link GWT#runAsync(Class, com.google.gwt.core.client.RunAsyncCallback)}.
 * 
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface LoadAsync {

  /**
   * The fragment name to group all types with the same name into the same code fragment.
   */
  Class<?> value() default org.jboss.errai.ioc.client.api.LoadAsync.NO_FRAGMENT.class;

  public static abstract class NO_FRAGMENT {};
}
