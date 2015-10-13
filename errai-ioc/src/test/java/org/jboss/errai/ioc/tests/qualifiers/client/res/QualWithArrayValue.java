package org.jboss.errai.ioc.tests.qualifiers.client.res;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
public @interface QualWithArrayValue {

  String[] value();

}
