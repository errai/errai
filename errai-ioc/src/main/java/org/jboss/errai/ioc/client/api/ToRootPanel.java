package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An {@link com.google.gwt.user.client.ui.Panel} object annotated with this annotation will be automatically added
 * to the RootPanel or the document root of the DOM in the application. It generally should only be used on the main
 * outer container, as each annotated class will simply be passed to <tt>RootPanel.get().add(...)</tt>
 *
 * Example:
 * <pre><code>
 * @ToRootPanel
 * public class MyRootPanel extends VerticalPanel {
 *      public MyRootPanel() {
 *          // setup panel ...
 *      }
 * }
 * </code></pre>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ToRootPanel {
}
