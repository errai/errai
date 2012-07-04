package org.jboss.errai.ioc.tests.unit;

import java.lang.annotation.Annotation;
import java.util.Collections;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.meta.impl.build.BuildMetaClass;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.ioc.tests.wiring.client.res.ConstructorInjectedBean;
import org.jboss.errai.ioc.tests.wiring.client.res.FooService;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.StringSourceWriter;

public class InjectionPointTest extends TestCase {

  /**
   * Tests that it is safe to call ensureMemberExposed() on any type of
   * InjectionPoint. (This was an actual bug that was not caught by the
   * existing integration tests).
   */
  public void testEnsureMemberExposedWithConstructorInjectionPoint() throws Exception {
    IOCProcessingContext processingContext = IOCProcessingContext.Builder.create()
            .logger(
              new TreeLogger() {
                @Override
                public TreeLogger branch(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
                  return null;
                }

                @Override
                public boolean isLoggable(Type type) {
                  return false;
                }

                @Override
                public void log(Type type, String msg, Throwable caught, HelpInfo helpInfo) {
                  System.out.println(type.getLabel() + ": " + msg);
                  if (caught != null) {
                    caught.printStackTrace();
                  }
                }
              })
            .sourceWriter(new StringSourceWriter())
            .context(Context.create())
            .bootstrapClassInstance(new BuildMetaClass(Context.create(), "FakeBootstrapper"))
            .blockBuilder(Stmt.do_())
            .packages(Collections.singleton(ConstructorInjectedBean.class.getPackage().getName()))
            .build();
    InjectionContext ctx = InjectionContext.Builder.create().processingContext(processingContext).build();
    MetaConstructor constructor = MetaClassFactory.get(ConstructorInjectedBean.class).getConstructor(FooService.class);
    InjectionPoint<Inject> injectionPoint = new InjectionPoint<Inject>(new Inject() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Inject.class;
      }
    }, TaskType.Parameter, constructor,
    null, null, null, constructor.getParameters()[0], null, ctx);

    // holy crap that was a lot of setup. Here comes the actual test:

    injectionPoint.ensureMemberExposed();
  }

}
