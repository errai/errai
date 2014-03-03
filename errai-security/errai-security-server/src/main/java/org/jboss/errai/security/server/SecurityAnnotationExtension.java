package org.jboss.errai.security.server;

import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * This {@link Extension} allows type level {@link RequireAuthentication} and
 * {@link RequireRoles} annotations to trigger server-side interceptors on their
 * method calls.
 * 
 * @author edewit@redhat.com
 */
public class SecurityAnnotationExtension implements Extension {

  /**
   */
  public void addParameterLogger(@Observes ProcessAnnotatedType<?> processAnnotatedType) {
    final Class<?>[] interfaces = processAnnotatedType.getAnnotatedType().getJavaClass().getInterfaces();

    for (Class<?> anInterface : interfaces) {
      for (Method method : anInterface.getMethods()) {
        copyAnnotation(processAnnotatedType, method, RequireAuthentication.class);
        copyAnnotation(processAnnotatedType, method, RequireRoles.class);
      }
    }
  }

  private <X> void copyAnnotation(ProcessAnnotatedType<X> annotatedType, Method method,
          Class<? extends Annotation> annotation) {
    if (method.isAnnotationPresent(annotation)) {
      AnnotatedTypeBuilder<X> builder = new AnnotatedTypeBuilder<X>().readFromType(annotatedType.getAnnotatedType())
              .addToMethod(getMethod(annotatedType, method.getName()), method.getAnnotation(annotation));
      annotatedType.setAnnotatedType(builder.create());
    }
  }

  private <X> AnnotatedMethod<? super X> getMethod(ProcessAnnotatedType<X> annotatedType, String name) {
    for (AnnotatedMethod<? super X> annotatedMethod : annotatedType.getAnnotatedType().getMethods()) {
      if (name.equals(annotatedMethod.getJavaMember().getName())) {
        return annotatedMethod;
      }
    }
    throw new IllegalArgumentException("cannot find method on implementation class that is on the interface");
  }
}
