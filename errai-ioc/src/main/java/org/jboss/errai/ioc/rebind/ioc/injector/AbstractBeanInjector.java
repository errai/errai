package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

import javax.enterprise.inject.New;
import java.lang.annotation.Annotation;

import static org.jboss.errai.codegen.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.load;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

/**
 * @author Mike Brock
 */
public class AbstractBeanInjector extends AbstractInjector {
  protected final MetaClass type;
  protected String varName;

  protected AbstractBeanInjector(MetaClass type) {
    this.type = type;
  }

  protected static boolean hasNewQualifier(InjectableInstance instance) {
    if (instance != null) {
      for (Annotation annotation : instance.getQualifiers()) {
        if (annotation.annotationType().equals(New.class)) return true;
      }
    }
    return false;
  }


  @Override
  public Statement getBeanInstance(InjectableInstance injectableInstance) {
    return null;
  }

  protected Statement generateCreationalContext(BeanInstantiationCallback beanInstantiationCallback,
                                                InjectableInstance injectableInstance) {

    // check to see if this injector has already been injected
    if (isCreated()) {
      if (isSingleton() && !hasNewQualifier(injectableInstance)) {

        /*
        if this bean is a singleton bean and there is no @New qualifier on the site we're injecting
        into, we mearly return a reference to the singleton instance variable from the bootstrapper.
        */
        return Refs.get(varName);
      }
      else if (creationalCallbackVarName != null) {

        /*
        if the bean is not singleton, or it's scope is overridden to be effectively dependent,
        we return a call CreationContext.getInstance() on the CreationalContext for this injector.
        */
        return loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
      }
    }

    final InjectionContext injectContext = injectableInstance.getInjectionContext();
    final IOCProcessingContext ctx = injectContext.getProcessingContext();

    /*
    get a parameterized version of the CreationalCallback class, parameterized with the type of
    bean it produces.
    */
    final MetaClass creationCallbackRef = parameterizedAs(CreationalCallback.class, typeParametersOf(type));

    /*
    begin building the creational callback, implement the "getInstance" method from the interface
    and assign its BlockBuilder to a callbackBuilder so we can work with it.
    */
    final BlockBuilder<AnonymousClassStructureBuilder> callbackBuilder
            = newInstanceOf(creationCallbackRef).extend()
            .publicOverridesMethod("getInstance", Parameter.of(CreationalContext.class, "context", true));

    /*
    render local variables Class::beanType and Annotation[]::qualifiers at the beginning of the getInstance()
    method so we can easily refer to them later on.
    */
    callbackBuilder.append(declareVariable(Class.class).named("beanType").initializeWith(load(type)));
    callbackBuilder.append(declareVariable(Annotation[].class).named("qualifiers")
            .initializeWith(load(qualifyingMetadata.getQualifiers())));


    /* push the method block builder onto the stack, so injection tasks are rendered appropriately. */
    ctx.pushBlockBuilder(callbackBuilder);

    /* get a new unique variable for the creational callback */
    creationalCallbackVarName = InjectUtil.getNewInjectorName() + "_" + type.getName() + "_creationalCallback";

    beanInstantiationCallback.instantiateBean(injectContext, callbackBuilder);
    
    /*
    return the instance of the bean from the creational callback.
    */
    callbackBuilder.append(loadVariable(varName).returnValue());

    /* pop the block builder of the stack now that we're done wiring. */
    ctx.popBlockBuilder();


    /*
    declare a final variable for the CreationalCallback and initialize it with the anonymous class we just
    built.
    */
    ctx.globalAppend(declareVariable(creationCallbackRef).asFinal().named(creationalCallbackVarName)
            .initializeWith(callbackBuilder.finish().finish()));

    Statement retVal;

    if (isSingleton()) {
      /*
       if the injector is for a singleton, we create a variable to hold the singleton reference in the bootstrapper
       method and assign it with CreationalContext.getInstance().
       */
      ctx.globalAppend(declareVariable(type).asFinal().named(varName)
              .initializeWith(loadVariable(creationalCallbackVarName).invoke("getInstance",
                      Refs.get("context"))));

      /*
       use the variable we just assigned as the return value for this injector.
       */
      retVal = Refs.get(varName);
    }
    else {
      /*
       the injector is a dependent scope, so use CreationContext.getInstance() as the return value.
       */
      retVal = loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"));
    }

    /*
      notify any component waiting for this type that is is ready now.
     */

    setRendered(true);

    injectableInstance.getInjectionContext().getProcessingContext()
            .handleDiscoveryOfType(injectableInstance);
    /*
      return the reference to this bean to whoever called us.
     */
    return retVal;
  }


  @Override
  public MetaClass getInjectedType() {
    return null;
  }

  @Override
  public String getVarName() {
    return varName;
  }
}
