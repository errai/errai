package org.jboss.errai.security.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.security.client.local.identity.Identity;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.shared.api.style.AnnotationStyleBindingExecutor;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.lang.annotation.Annotation;

/**
 * RoleStyleBindingProvider makes sure that client elements annotated by {@link RequireRoles} are made invisible for
 * users that do not have the role or roles specified.
 *
 * @see RequireRoles
 * @author edewit@redhat.com
 */
@Singleton
public class RoleStyleBindingProvider {

  private final Identity identity;

  @Inject
  public RoleStyleBindingProvider(Identity identity) {
    this.identity = identity;
  }

  @AfterInitialization
  public void init() {
    StyleBindingsRegistry.get().addStyleBinding(this, RequireRoles.class, new AnnotationStyleBindingExecutor() {
      @Override
      public void invokeBinding(final Element element, final Annotation annotation) {
        identity.hasPermission(new AsyncCallback<Boolean>() {

          @Override
          public void onSuccess(Boolean result) {
            element.getStyle().setVisibility(result ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
          }

          @Override
          public void onFailure(Throwable caught) {
          }
        }, ((RequireRoles) annotation).value());
      }
    });
  }
}
