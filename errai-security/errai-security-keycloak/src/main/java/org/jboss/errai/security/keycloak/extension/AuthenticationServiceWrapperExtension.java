package org.jboss.errai.security.keycloak.extension;

import java.lang.annotation.Annotation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jboss.errai.security.keycloak.KeycloakAuthenticationService;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * This CDI Extension allows the {@link KeycloakAuthenticationService} in this project to wrap an
 * {@link AuthenticationService} already on the classpath so that the
 * {@link KeycloakAuthenticationService} can be used to extend existing security infrastructure.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class AuthenticationServiceWrapperExtension implements Extension {

  private final Wrapped wrapped = new Wrapped() {
                @Override
                public Class<? extends Annotation> annotationType() {
                  return Wrapped.class;
                }
              };

  public <T> void processAuthenticationServiceTypes(@Observes final ProcessAnnotatedType<T> processedAnnotatedType) {
    final Class<T> type = processedAnnotatedType.getAnnotatedType().getJavaClass();

    if (isNonKeycloakAuthenticationService(type)) {
      processedAnnotatedType.setAnnotatedType(new AnnotatedTypeBuilder<T>()
              .readFromType(processedAnnotatedType.getAnnotatedType()).removeFromClass(Default.class)
              .addToClass(wrapped).create());
    }
  }

  private <T> boolean isNonKeycloakAuthenticationService(final Class<T> type) {
    return AuthenticationService.class.isAssignableFrom(type)
            && !KeycloakAuthenticationService.class.isAssignableFrom(type);
  }
}
