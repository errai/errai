package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * An annotation that provides a mechanism for the developer to specify the name
 * of an i18n bundle to use when doing i18n replacements within an Errai UI Template.
 * Note that multiple bundles can be used, but all bundles are ultimately aggregated
 * together prior to translating any of the Errai UI Templates.  In other words,
 * developers are free to break up their bundles in whatever way makes sense, but
 * at the end of the day all bundles will be merged and indexed by locale.
 * </p>
 *
 * <pre>
 * package org.example;
 *
 * &#064;Dependent
 * &#064;Templated
 * &#064;Bundle("loginBundle.json")
 * public class CustomComponent extends Composite
 * {
 *    &#064;Inject &#064;DataField("username.label")
 *    private InlineLabel usernameLabel;
 *    &#064;Inject
 *    private TextBox username;
 *
 *    &#064;Inject &#064;DataField("password.label")
 *    private InlineLabel passwordLabel;
 *    &#064;Inject
 *    private TextBox password;
 *
 *    &#064;Inject &#064;DataField("login")
 *    private Button login;
 *
 *    &#064;Inject &#064;DataField("cancel")
 *    private Button cancel;
 *
 *    &#064;EventHandler(&quot;login&quot;)
 *    private void doLogin(ClickEvent event)
 *    {
 *       // log in
 *    }
 * }
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bundle {

  /**
   * Indicates the name of the Errai i18n bundle to use for i18n replacements
   * for this class.
   */
  String value();

}