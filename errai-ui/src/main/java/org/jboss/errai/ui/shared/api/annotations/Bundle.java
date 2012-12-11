package org.jboss.errai.ui.shared.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * An annotation that provides a mechanism for the developer to specify the name
 * of an i18n bundle to use when doing i18n replacements within the annotated
 * element. Errai UI's internationalization support includes the 'i18n'
 * attribute that can be used in Templates as well as the @I18N annotation that
 * can be used on fields and parameters.
 * </p>
 *
 * <p>
 * If this annotation is present on an element, then the i18n bundle indicated by
 * the annotation will be used as the source of the i18n data. If this
 * annotation is not present on an element, then the default bundle is used (the
 * default bundle name is <b>erraiBundle</b>).
 * </p>
 *
 * <p>
 * Note that a bundle must be present as a JSON map assigned to a javascript
 * variable. This variable can be included in the host page or downloaded
 * separately via a javascript include.
 * </p>
 *
 * <pre>
 * package org.example;
 *
 * &#064;Bundle("loginBundle")
 * public class CustomComponent extends FlowPanel
 * {
 *    &#064;Inject &#064;I18N("username.label")
 *    private InlineLabel usernameLabel;
 *    &#064;Inject
 *    private TextBox username;
 *
 *    &#064;Inject &#064;I18N("password.label")
 *    private InlineLabel passwordLabel;
 *    &#064;Inject
 *    private TextBox password;
 *
 *    &#064;Inject &#064;I18N("login")
 *    private Button login;
 *
 *    &#064;Inject &#064;I18N("cancel")
 *    private Button cancel;
 *
 *    &#064;EventHandler(&quot;login&quot;)
 *    private void doLogin(ClickEvent event)
 *    {
 *       // log in
 *    }
 *
 *    &#064;PostConstruct
 *    private void init() {
 *       add(usernameLabel);
 *       add(username);
 *       add(passwordLabel);
 *       add(password);
 *       add(login);
 *       add(cancel);
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
