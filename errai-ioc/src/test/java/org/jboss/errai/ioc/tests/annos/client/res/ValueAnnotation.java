package org.jboss.errai.ioc.tests.annos.client.res;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Mike Brock
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueAnnotation {
  String value();
}
