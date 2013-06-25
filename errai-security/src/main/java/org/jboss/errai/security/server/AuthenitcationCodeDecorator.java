package org.jboss.errai.security.server;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.impl.ObjectBuilder;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.api.CodeDecorator;
import org.jboss.errai.ioc.rebind.ioc.extension.IOCDecoratorExtension;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.security.client.local.SecurityInterceptor;
import org.jboss.errai.security.client.local.SecurityRoleInterceptor;
import org.jboss.errai.security.client.local.SecurityUserInterceptor;
import org.jboss.errai.security.shared.RequireAuthentication;
import org.jboss.errai.security.shared.RequireRoles;
import org.jboss.errai.ui.nav.client.local.Page;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@CodeDecorator
public class AuthenitcationCodeDecorator extends IOCDecoratorExtension<Page> {

  public AuthenitcationCodeDecorator(Class<Page> decoratesWith) {
    super(decoratesWith);
  }

  @Override
  public List<? extends Statement> generateDecorator(InjectableInstance<Page> ctx) {
    final List<Statement> stmts = new ArrayList<Statement>();
    if (ctx.getTaskType() == TaskType.Type) {
      final Annotation[] annotations = ctx.getElementType().getAnnotations();
      if (isRequireRoleAnnotated(annotations)) {
        createInterceptor(stmts, SecurityRoleInterceptor.class);
        stmts.add(Stmt.loadVariable("interceptor").invoke(
                "securityCheck", getAnnotation(annotations, RequireRoles.class).value(), null)
        );
      }
      if (isRequireAuthentication(annotations)) {
        createInterceptor(stmts, SecurityUserInterceptor.class);
        stmts.add(Stmt.loadVariable("interceptor").invoke("securityCheck")
        );
      }
    }

    return stmts;
  }

  private void createInterceptor(List<Statement> stmts, Class<? extends SecurityInterceptor> interceptorClass) {
    final ObjectBuilder builder = ObjectBuilder.newInstanceOf(interceptorClass);
    stmts.add(Stmt.declareFinalVariable("interceptor", interceptorClass, builder));
  }

  private boolean isRequireAuthentication(Annotation[] annotations) {
    return getAnnotation(annotations, RequireAuthentication.class) != null;
  }

  private boolean isRequireRoleAnnotated(Annotation[] annotations) {
    return getAnnotation(annotations, RequireRoles.class) != null;
  }

  @SuppressWarnings("unchecked")
  private <T> T getAnnotation(Annotation[] annotations, Class<T> toSearchFor) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().equals(toSearchFor)) {
        return (T) annotation;
      }
    }
    return null;
  }
}
