package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * Defines an injector which is responsible for providing instance references of beans to the code generating
 * container..
 *
 * @author Mike Brock
 */
public interface Injector {
  Statement getBeanInstance(InjectableInstance injectableInstance);

  /**
   * Checks if the injector represents a test mock.
   * @return true if the injector is a test mock
   */
  boolean isTestmock();

  /**
   * Checks if the injector an alternative.
   * @return true if the injector is an alternative
   */
  boolean isAlternative();

  /**
   * Checks if the injector's CreationalCallback already been rendered.
   * @return true if the creational callback has already been rendered.
   */
  boolean isRendered();

  /**
   * Checks if construction has begun.
   * @return
   */
  public boolean isCreated();

  /**
   * Checks if the injector for a singleton bean.
   * @return true if the injector handles a singleton bean.
   */
  boolean isSingleton();

  /**
   * Check if the injector if of the dependent scope.
   * @return true if the injector is of a dependent scope.
   */
  boolean isDependent();

  /**
   * Checks if the injector is a psuedo bean. A psuedo bean is a bean which has been discovered in the classpath
   * and is by default considered part of the dependent scope. But it's scope may be overridden through further
   * processing.
   *
   * @return true if the injector is for a psuedo bean.
   */
  boolean isPseudo();

  /**
   * Checks if the injector represents a provider.
   *
   * @return true if the injector is for a provider.
   */
  boolean isProvider();


  /**
   * Checks is the injector is a static injector, meaning that it can safely be referenced from anywhere, without
   * concern for ordering of its declaring class.
   *
   * @return true if the injector is a static injector
   */
  boolean isStatic();

  /**
   * The enclosing type of the injector. For producer injectors, this method will return the bean which the
   * producer method is a member.
   * @return the enclosing bean type of the injector, if applicable. Null if not applicable.
   */
  MetaClass getEnclosingType();


  /**
   * The injected type of the injector. This is the absolute type which the injector produces. For producers, this
   * is the bean type which the producer method returns.
   * @return the absolute return type from the injector.
   */
  MetaClass getInjectedType();


  /**
   * The unique variable name for the bean instance. Usually used to reference the bean during the wiring of the
   * bean within the CreationalContext.getInstance() method body. This variable name is also used to provide
   * a name to variable which holds a refeerence to singleton isntances.
   *
   * @return the unique variable name for a bean in the bootstrapper and CreationalContext.getInstance() method.
   */
  String getVarName();

  /**
   * The unique variable name for the InitalizationCallback associated with a bean CreationalContext in the
   * boostrapper method.
   *
   * @return the unique variable name for an InitializationCallback instance. Null if none.
   */
  String getPostInitCallbackVar();

  /**
   * Sets a variable name reference to the InitializationCallback to associate with the CreationalCallback for this bean.
   *
   * @param var a unique variable name pointing to an instance of InitializationCallback.
   */
  void setPostInitCallbackVar(String var);

  /**
   * The unique variable name for the DestructionCallback associated with the CreationalCallback for this bean.
   *
   * @return the unique variable name for a DestructionCallback instance. Null if none.
   */
  String getPreDestroyCallbackVar();

  /**
   * Sets a variable name reference to the DestructionCallback to associate with this CreationalCallback for this bean.
   *
   * @param preDestroyCallbackVar a unique variable name pointing to an instance of InitializationCallback
   */
  void setPreDestroyCallbackVar(String preDestroyCallbackVar);

  /**
   * The unique variable name for a CreationalCallback associated with this bean.
   *
   * @return the unique variable name for the CreationalCallback.
   */
  public String getCreationalCallbackVarName();

  /**
   * Determines whether or not the the bean type this injector producers matches the specified parameterized type
   * and qualifying metadata.
   *
   * @param parameterizedType the parameterized type to compare against.
   * @param qualifyingMetadata the qualifying metadata to compare against
   * @return true if matches.
   */
  boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata);

  /**
   * Returns the QualifyingMetadata associated with this injector.
   * @return the qualifying meta data.
   */
  QualifyingMetadata getQualifyingMetadata();

  /**
   * Returns parameterized type data associated with this injector
   * @return parameterized type associated with this injector. Null if none.
   */
  MetaParameterizedType getQualifyingTypeInformation();

  /**
   * Adds a registration hook to be triggered when the bean is ready to render its registration to be bean manager
   * @param registrationHook a registration hook to be called at registration of the bean with the bean manager.
   */
  void addRegistrationHook(RegistrationHook registrationHook);

  String getBeanName();
}
