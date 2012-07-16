/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.rebind.ioc.injector;

import static org.jboss.errai.codegen.builder.impl.ObjectBuilder.newInstanceOf;
import static org.jboss.errai.codegen.meta.MetaClassFactory.parameterizedAs;
import static org.jboss.errai.codegen.meta.MetaClassFactory.typeParametersOf;
import static org.jboss.errai.codegen.util.Stmt.declareVariable;
import static org.jboss.errai.codegen.util.Stmt.load;
import static org.jboss.errai.codegen.util.Stmt.loadVariable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.New;

import org.jboss.errai.codegen.Parameter;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.builder.AnonymousClassStructureBuilder;
import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.util.Refs;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.CreationalCallback;
import org.jboss.errai.ioc.client.container.CreationalContext;
import org.jboss.errai.ioc.rebind.ioc.bootstrapper.IOCProcessingContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.ConstructionStatusCallback;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.WiringElementType;
import org.mvel2.util.NullType;

/**
 * This injector implementation is responsible for the lion's share of the container's workload. It is responsible
 * for generating the <tt>CreationalContext</tt>'s which produce instances of beans. It is also responsible for
 * handling the differences in semantics between singleton and dependent-scoped beans.
 *
 * @author Mike Brock
 */
public class TypeInjector extends AbstractInjector {
  protected final MetaClass type;
  protected String varName;

  public TypeInjector(MetaClass type, InjectionContext context) {
    this(type, context, new Annotation[0]);
  }

  public TypeInjector(MetaClass type, InjectionContext context, Annotation[] additionalQualifiers) {
    this.type = type;

    if (type.getFullyQualifiedName().equals(NullType.class.getName())) {
      new Throwable().printStackTrace();
    }

    // check to see if this is a singleton and/or alternative bean

    this.testmock = context.isElementType(WiringElementType.TestMockBean, type);
    this.singleton = context.isElementType(WiringElementType.SingletonBean, type);
    this.alternative = context.isElementType(WiringElementType.AlternativeBean, type);

    this.varName = InjectUtil.getNewInjectorName() + "_" + type.getName();

    Set<Annotation> qualifiers = new HashSet<Annotation>();
    qualifiers.addAll(InjectUtil.getQualifiersFromAnnotations(type.getAnnotations()));
    qualifiers.addAll(Arrays.asList(additionalQualifiers));

    if (!qualifiers.isEmpty()) {
      qualifyingMetadata = context.getProcessingContext().getQualifyingMetadataFactory()
              .createFrom(qualifiers.toArray(new Annotation[qualifiers.size()]));

    }
    else {
      qualifyingMetadata = context.getProcessingContext().getQualifyingMetadataFactory().createDefaultMetadata();
    }
  }

  @Override
  public Statement getBeanInstance(final InjectableInstance injectableInstance) {
    final Statement val = _getType(injectableInstance);
    registerWithBeanManager(injectableInstance.getInjectionContext(), val);
    return val;
  }

  private Statement _getType(final InjectableInstance injectableInstance) {
    // check to see if this injector has already been injected
    if (isCreated()) {
      if (isSingleton() && !hasNewQualifier(injectableInstance)) {

        /*
        if this bean is a singleton bean and there is no @New qualifier on the site we're injecting
        into, we merely return a reference to the singleton instance variable from the bootstrapper.
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
    callbackBuilder
            ._(declareVariable(Class.class).named("beanType").initializeWith(load(type)))
            ._(declareVariable(Annotation[].class).named("qualifiers")
                    .initializeWith(load(qualifyingMetadata.getQualifiers())));


    /* push the method block builder onto the stack, so injection tasks are rendered appropriately. */
    ctx.pushBlockBuilder(callbackBuilder);

    /* get a new unique variable for the creational callback */
    creationalCallbackVarName = InjectUtil.getNewInjectorName() + "_" + type.getName() + "_creationalCallback";

    /* get the construction strategy and execute it to wire the bean */
    InjectUtil.getConstructionStrategy(this, injectContext).generateConstructor(new ConstructionStatusCallback() {
      @Override
      public void beanConstructed() {
        /* the bean has been constructed, so get a reference to the BeanRef and set it to the 'beanRef' variable. */

        callbackBuilder.append(declareVariable(BeanRef.class).named("beanRef")
                .initializeWith(loadVariable("context").invoke("getBeanReference", Refs.get("beanType"),
                        Refs.get("qualifiers"))));

        /* add the bean to CreationalContext */
        callbackBuilder.append(loadVariable("context").invoke("addBean", Refs.get("beanRef"), Refs.get(varName)));

        /* mark this injector as injected so we don't go into a loop if there is a cycle. */
        setCreated(true);
      }
    });

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
//    ctx.globalAppend(declareVariable(creationCallbackRef).asFinal().named(creationalCallbackVarName)
//            .initializeWith(callbackBuilder.finish().finish()));

    ctx.getBootstrapBuilder().privateField(creationalCallbackVarName, creationCallbackRef).modifiers(Modifier.Final)
            .initializesWith(callbackBuilder.finish().finish()).finish();

    Statement retVal;

    if (isSingleton()) {
      /*
       if the injector is for a singleton, we create a variable to hold the singleton reference in the bootstrapper
       method and assign it with CreationalContext.getInstance().
       */
//      ctx.globalAppend(declareVariable(type).asFinal().named(varName)
//              .initializeWith(loadVariable(creationalCallbackVarName).invoke("getInstance",
//                      Refs.get("context"))));

      ctx.getBootstrapBuilder().privateField(varName, type).modifiers(Modifier.Final)
              .initializesWith(loadVariable(creationalCallbackVarName).invoke("getInstance", Refs.get("context"))).finish();

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

  private static boolean hasNewQualifier(final InjectableInstance instance) {
    if (instance != null) {
      for (final Annotation annotation : instance.getQualifiers()) {
        if (annotation.annotationType().equals(New.class)) return true;
      }
    }
    return false;
  }

  public boolean isPseudo() {
    return replaceable;
  }

  @Override
  public String getVarName() {
    return varName;
  }

  @Override
  public MetaClass getInjectedType() {
    return type;
  }

  public String getCreationalCallbackVarName() {
    return creationalCallbackVarName;
  }
}
