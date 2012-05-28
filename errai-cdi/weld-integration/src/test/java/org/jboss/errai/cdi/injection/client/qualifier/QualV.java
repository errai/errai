package org.jboss.errai.cdi.injection.client.qualifier;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mike Brock
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface QualV {
  QualEnum value();

  @Nonbinding int amount() default 0;
}
