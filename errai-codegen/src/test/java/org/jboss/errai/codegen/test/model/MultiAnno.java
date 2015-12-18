package org.jboss.errai.codegen.test.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mike Brock
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiAnno {
  String[] value();

  int age();
}
