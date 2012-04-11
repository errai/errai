package org.jboss.errai.common.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that <i>in GWT Development Mode</i>, the target type should not be
 * exposed to the web application within which it is deployed. Presently, this
 * is only enforced when the application is launched via the JettyLauncher class
 * in the errai-cdi-jetty project. If necessary, other Dev Mode environments may
 * choose to enforce this class hiding annotation in the future.
 * <p>
 * <b>Motivation:</b> we have to hide unloadable classes from the classpath scan
 * performed by Weld, Hibernate, or any other component that tries to load every
 * class. Class load errors can cause the scan to fail completely, meaning the
 * webapp doesn't initialize.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HiddenFromDevModeWebappContext {
}
