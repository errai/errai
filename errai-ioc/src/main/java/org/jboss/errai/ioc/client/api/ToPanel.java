package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assign the {@link com.google.gwt.user.client.ui.Panel} object to a panel with the specified name.  
 *
 * @see org.jboss.errai.ioc.client.api.CreatePanel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToPanel {
    String value();
}
