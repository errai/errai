package org.jboss.errai.config.rebind;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mike Brock
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateAsync {
  Class value();
}
