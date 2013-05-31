package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.ProxyMaker;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RenderingHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

import java.util.Map;

/**
 * Defines an injector which is responsible for providing instance references of beans to the code generating
 * container.
 *
 * @author Mike Brock
 */
public interface Injector {
  void renderProvider(InjectableInstance injectableInstance);

  /**
   * Return a statement providing access to the injector (or null for asynchronous logic)
   *
   * @param injectableInstance
   *
   * @return
   */
  Statement getBeanInstance(InjectableInstance injectableInstance);

  /**
   * Checks if the injector is enabled, and is eligible for injection consideration.
   *
   * @return true if the injector is enabled
   */
  boolean isEnabled();

  /**
   * Checks if the injector is soft disabled. This is an optimization flag, allowing for the code optimizers to
   * indicate they'd like to remove the underlying bean. Soft disabled injectors will always report as not being
   * enabled, but are subject to reactivation if they are requested for injection.
   *
   * @return true if the injector is soft disabled.
   */
  boolean isSoftDisabled();

  /**
   * Checks if the injector represents a test mock.
   *
   * @return true if the injector is a test mock
   */
  boolean isTestMock();

  /**
   * Checks if the injector an alternative.
   *
   * @return true if the injector is an alternative
   */
  boolean isAlternative();

  /**
   * Checks if the injector's BeanProvider already been rendered.
   *
   * @return true if the creational callback has already been rendered.
   */
  boolean isRendered();

  /**
   * Checks if construction has begun.
   *
   * @return
   */
  public boolean isCreated();

  /**
   * Checks if the injector for a singleton bean.
   *
   * @return true if the injector handles a singleton bean.
   */
  boolean isSingleton();

  /**
   * Check if the injector if of the dependent scope.
   *
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
   *
   * @return the enclosing bean type of the injector, if applicable. Null if not applicable.
   */
  MetaClass getEnclosingType();


  /**
   * The injected type of the injector. This is the absolute type which the injector produces. For producers, this
   * is the bean type which the producer method returns.
   *
   * @return the return type from the injector.
   */
  MetaClass getInjectedType();

  /**
   * Returns the concrete type that will be returned by this injector.
   *
   * @return
   */
  MetaClass getConcreteInjectedType();


  /**
   * The unique variable name for the bean instance. Usually used to reference the bean during the wiring of the
   * bean within the CreationalContext.getInstance() method body. This variable name is also used to provide
   * a name to variable which holds a reference to singleton instances.
   *
   * @return the unique variable name for a bean in the bootstrapper and CreationalContext.getInstance() method.
   */
  String getInstanceVarName();


  /**
   * The unique variable name for the proxied bean instance.
   *
   * @return the unique variable name for the proxy of the bean in the bootstrapper.
   */
  String getProxyInstanceVarName();

  /**
   * The unique variable name for the InitalizationCallback associated with a bean CreationalContext in the
   * boostrapper method.
   *
   * @return the unique variable name for an InitializationCallback instance. Null if none.
   */
  String getPostInitCallbackVar();

  /**
   * Sets a variable name reference to the InitializationCallback to associate with the BeanProvider for this bean.
   *
   * @param var
   *     a unique variable name pointing to an instance of InitializationCallback.
   */
  void setPostInitCallbackVar(String var);

  /**
   * The unique variable name for the DestructionCallback associated with the BeanProvider for this bean.
   *
   * @return the unique variable name for a DestructionCallback instance. Null if none.
   */
  String getPreDestroyCallbackVar();

  /**
   * Sets a variable name reference to the DestructionCallback to associate with this BeanProvider for this bean.
   *
   * @param preDestroyCallbackVar
   *     a unique variable name pointing to an instance of InitializationCallback
   */
  void setPreDestroyCallbackVar(String preDestroyCallbackVar);

  /**
   * The unique variable name for a BeanProvider associated with this bean.
   *
   * @return the unique variable name for the BeanProvider.
   */
  public String getCreationalCallbackVarName();

  /**
   * Determines whether or not the the bean type this injector producers matches the specified parameterized type
   * and qualifying metadata.
   *
   * @param parameterizedType
   *     the parameterized type to compare against.
   * @param qualifyingMetadata
   *     the qualifying metadata to compare against
   *
   * @return true if matches.
   */
  boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata);

  /**
   * Returns the QualifyingMetadata associated with this injector.
   *
   * @return the qualifying meta data.
   */
  QualifyingMetadata getQualifyingMetadata();

  /**
   * Returns parameterized type data associated with this injector
   *
   * @return parameterized type associated with this injector. Null if none.
   */
  MetaParameterizedType getQualifyingTypeInformation();

  /**
   * Adds a registration hook to be triggered when the bean is ready to render its registration to be bean manager
   *
   * @param registrationHook
   *     a registration hook to be called at registration of the bean with the bean manager.
   */
  void addRegistrationHook(RegistrationHook registrationHook);

  /**
   * Adds a {@link RenderingHook} which will be triggered when the injector is rendered.
   *
   * @param renderingHook
   *     an instance of {@link RenderingHook} to be called when the injector is rendered.
   */
  void addRenderingHook(RenderingHook renderingHook);

  /**
   * Add a {@link Runnable} task to be executed when and if the injector is disabled.
   *
   * @param runnable
   *     an instance of {@link Runnable} to be called if the injector is disabled.
   */
  void addDisablingHook(Runnable runnable);

  /**
   * Get the name of the bean (if it has a name). Otherwise return null.
   *
   * @return the name of the bean. or null if it has no name.
   */
  String getBeanName();

  /**
   * Set the enabled state of the bean. A bean that has been disabled (set to <tt>false</tt>), is not eligible for
   * injection.
   *
   * @param enabled
   *     the enabled state of the bean to set (<tt>true</tt> for enabled, <tt>false</tt> for disabled).
   */
  void setEnabled(boolean enabled);

  /**
   * Returns true if the injector type is a regular type injector. (ie. not a proxy injector, producer injector, etc).
   *
   * @return true if the injector is a regular type injector.
   */
  boolean isRegularTypeInjector();

  /**
   * Sets a persistent attribute to be associated with this injector.
   *
   * @param name the name of the attribute
   * @param value the value of the attribute.
   */
  public void setAttribute(String name, Object value);


  /**
   * Gets a persistent attribute associated with this injector.
   *
   * @param name the name of the attribute
   * @return the value of the attribute. null if the attribute does not exist.
   */
  public Object getAttribute(String name);

  /**
   * Checks if injector has the specified named attribute
   *
   * @param name the name of the attribute.
   * @return true if the attribute exists.
   */
  public boolean hasAttribute(String name);

  /**
   * Checks if the injector is a proxied injector. That is to say, the value returned from generated
   * {@link org.jboss.errai.ioc.client.container.BeanProvider}
   * returns a proxy, rather than a direct reference to a bean.
   *
   * @return true if the injector returns a proxied instance.
   */
  public boolean isProxied();

  /**
   * Adds a statement to be appended to the end of the generated {@link org.jboss.errai.ioc.client.container.BeanProvider}
   * code. Statements added here will be executed after all bean wiring activity has finished.
   *
   * @param statement
   */
  public void addStatementToEndOfInjector(Statement statement);


  /**
   * Adds an invoke around statement on the specified method.
   *
   * Calling this method automatically converts this injector into a proxied injector, as AOP activities must be done
   * through the creation of a proxy.

   * @param method the method to invoke around
   * @param statement the statement to execute.
   */
  void addInvokeAround(MetaMethod method, Statement statement);


  /**
   * Adds an invoke before statement on the specified method.
   *
   * Calling this method automatically converts this injector into a proxied injector, as AOP activities must be done
   * through the creation of a proxy.

   * @param method the method to invoke around
   * @param statement the statement to execute.
   */
  void addInvokeBefore(MetaMethod method, Statement statement);


  /**
   * Adds an invoke after statement on the specified method.
   *
   * Calling this method automatically converts this injector into a proxied injector, as AOP activities must be done
   * through the creation of a proxy.

   * @param method the method to invoke around
   * @param statement the statement to execute.
   */
  void addInvokeAfter(MetaMethod method, Statement statement);


  /**
   * Adds a proxy property to the generated proxy. Effectively this means the proxy will be given an instance
   * field to hold the value yielded by the specified statement.
   *
   * @param propertyName
   *        the name of the property.
   * @param type
   *        the type of the property.
   * @param statement
   *        the statement which will yield the value to be put into the property.
   *
   * @return
   *        a {@link org.jboss.errai.codegen.ProxyMaker.ProxyProperty} reference which can be used as a regular
   *        statement reference in Errai Codegen. The instance of ProxyProperty can be used in generated code
   *        (such as in AOP statements) to refer to the injected proxy property.
   */
  ProxyMaker.ProxyProperty addProxyProperty(String propertyName, Class type, Statement statement);

  /**
   * Adds a proxy property to the generated proxy. Effectively this means the proxy will be given an instance
   * field to hold the value yielded by the specified statement.
   *
   * @param propertyName
   *        the name of the property.
   * @param type
   *        the type of the property.
   * @param statement
   *        the statement which will yield the value to be put into the property.
   *
   * @return
   *        a {@link org.jboss.errai.codegen.ProxyMaker.ProxyProperty} reference which can be used as a regular
   *        statement reference in Errai Codegen. The instance of ProxyProperty can be used in generated code
   *        (such as in AOP statements) to refer to the injected proxy property.
   */
  ProxyMaker.ProxyProperty addProxyProperty(String propertyName, MetaClass type, Statement statement);


  /**
   * Returns a map of all proxy properties in the injector. The keys are the names of the property, and the values
   * are {@link org.jboss.errai.codegen.ProxyMaker.ProxyProperty} references.
   *
   * @return
   *      a map of proxy properties.
   */
  Map<String, ProxyMaker.ProxyProperty> getProxyPropertyMap();

  /**
   * This method should be called to ensure that the proxies have been updated to reflect any code weavings that may
   * have been added.
   */
  public void updateProxies();
}
