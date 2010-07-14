package org.jboss.errai.ioc.client.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instantiates and a panel and registers it with a specified name, which can be referred to by other components.
 *
 * <pre><code>
 * @CreatePanel("MyPanel")
 * public class SomePanel extends SimplePanel {
 *      ...
 * }
 *
 * </code></pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CreatePanel {
    String value() default "";
}
