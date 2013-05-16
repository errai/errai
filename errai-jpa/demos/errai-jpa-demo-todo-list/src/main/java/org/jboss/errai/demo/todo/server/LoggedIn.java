package org.jboss.errai.demo.todo.server;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * CDI qualifier for logged-in users.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoggedIn {

}
