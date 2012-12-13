package org.jboss.errai.ioc.client.api;

import com.google.gwt.resources.css.ast.CssProperty;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mike Brock
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnabledByProperty {
  String value();
  boolean negated() default false;
}
