package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that the target type, <em>which must be a subtype of Widget</em> is
 * a named page with optional state information within the Errai Navigation
 * system.
 *
 * @see TransitionTo
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Page {

  /**
   * The URI template that identifies this page. The template consists of two
   * components, both of which are optional:
   * <ol>
   *  <li>The page name. This is a literal string consisting of any combination
   *      of characters excluding / (forward slash) and &#123; (open brace bracket).
   *      If omitted, the page name is the same as the target class's simple name.
   *  <li>A template for the extra state information. This template consists of
   *      one or more URL variables of the form <tt>{name}</tt> (where name
   *      is a valid Java identifier) separated by / characters. If the template
   *      is omitted, the page has no extra state information.
   * </ol>
   *
   * <h4>Examples</h4>
   * <pre>
   *   {@code @Page}
   *   public class Product extends Composite {
   *     ...
   *   }</pre>
   * <p>The above example declares a page with no extra state information that can be
   * reached at {@code http://example.com/app/#Product} (the name defaults to the target
   * class's simple name).</p>
   * <br>
   * <pre>
   *   {@code @Page("products")}
   *   public class Product extends Composite {
   *     ...
   *   }</pre>
   * <p>The above example declares a page with no extra state information that can be
   * reached at {@code http://example.com/app/#products}.</p>
   * <br>
   * <pre>
   *   <tt>@Page("{prodId}/{photoId}")}</tt>
   *   public class Product extends Composite {
   *     ...
   *   }</pre>
   * <p>The above example declares a page with two pieces of extra state information
   * (called prodId and photoId) that can be reached at
   * {@code http://example.com/app/#Product/4/12}.</p>
   */
  String path() default "";

  /**
   * Indicates that the target class is the default starting page for the
   * application. This means the page can be reached from the empty path ("") as
   * well as the path it would normally be reachable by according to the
   * {@link #path()} specification.
   * <p>
   * In an Errai application that uses the navigation system, exactly one
   * {@code @Page}-annotated class must have {@code startingPage} set to
   * {@code true}. It is a compile-time error otherwise.
   */
  boolean startingPage() default false;
}
