package org.jboss.errai.ioc.tests.unit;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.user.rebind.StringSourceWriter;
import junit.framework.TestCase;
import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.builder.ClassStructureBuilder;
import org.jboss.errai.codegen.builder.impl.ClassBuilder;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.jboss.errai.codegen.util.Stmt;
import org.jboss.errai.ioc.client.SimpleInjectionContext;
import org.jboss.errai.ioc.client.container.SimpleCreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionPoint;
import org.jboss.errai.ioc.rebind.ioc.injector.api.TaskType;
import org.jboss.errai.ioc.tests.wiring.client.res.ConstructorInjectedBean;
import org.jboss.errai.ioc.tests.wiring.client.res.FooService;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Collections;

public class InjectionPointTest extends TestCase {

  /**
   * Tests that it is safe to call ensureMemberExposed() on any type of
   * InjectionPoint. (This was an actual bug that was not caught by the
   * existing integration tests).
   */
  public void testEnsureMemberExposedWithConstructorInjectionPoint() throws Exception {
    final ClassStructureBuilder<? extends ClassStructureBuilder<?>> structureBuilder = ClassBuilder.define("my.FakeBootstrapper").publicScope().body();

    final IOCProcessingContext processingContext = IOCProcessingContext.Builder.create()
        .logger(
            new TreeLogger() {
              @Override
              public TreeLogger branch(final Type type, final String msg, final Throwable caught, final HelpInfo helpInfo) {
                return null;
              }

              @Override
              public boolean isLoggable(final Type type) {
                return false;
              }

              @Override
              public void log(final Type type, final String msg, final Throwable caught, final HelpInfo helpInfo) {
                System.out.println(type.getLabel() + ": " + msg);
                if (caught != null) {
                  caught.printStackTrace();
                }
              }
            })
            //.sourceWriter(new StringSourceWriter())
        .context(Context.create())
        .bootstrapClassInstance(structureBuilder.getClassDefinition())
        .bootstrapBuilder(structureBuilder)
        .bootstrapContextClass(SimpleInjectionContext.class)
        .creationalContextClass(SimpleCreationalContext.class)
        .blockBuilder(Stmt.do_())
        .packages(Collections.singleton(ConstructorInjectedBean.class.getPackage().getName()))
        .build();
    final InjectionContext ctx = InjectionContext.Builder.create().processingContext(processingContext).build();
    final MetaConstructor constructor = MetaClassFactory.get(ConstructorInjectedBean.class).getConstructor(FooService.class);
    final InjectionPoint<Inject> injectionPoint = new InjectionPoint<Inject>(new Inject() {

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
