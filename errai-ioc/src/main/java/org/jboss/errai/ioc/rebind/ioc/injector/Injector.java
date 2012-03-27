package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaParameterizedType;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectableInstance;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;
import org.jboss.errai.ioc.rebind.ioc.injector.api.RegistrationHook;
import org.jboss.errai.ioc.rebind.ioc.metadata.QualifyingMetadata;

/**
 * @author Mike Brock
 */
public interface Injector {
  Statement getBeanInstance(InjectableInstance injectableInstance);

  Statement getBeanInstance(InjectionContext injectContext, InjectableInstance injectableInstance);

  boolean isTestmock();

  boolean isAlternative();

  boolean isInjected();

  boolean isSingleton();

  boolean isDependent();

  boolean isPseudo();

  boolean isProvider();

  MetaClass getEnclosingType();

  String getVarName();

  MetaClass getInjectedType();

  String getPostInitCallbackVar();

  void setPostInitCallbackVar(String var);

  String getPreDestroyCallbackVar();

  void setPreDestroyCallbackVar(String preDestroyCallbackVar);

  public String getCreationalCallbackVarName();

  public void setCreationalCallbackVarName(String creationalCallbackVarName);

  boolean metadataMatches(Injector injector);

  boolean matches(MetaParameterizedType parameterizedType, QualifyingMetadata qualifyingMetadata);

  QualifyingMetadata getQualifyingMetadata();

  MetaParameterizedType getQualifyingTypeInformation();

  void setQualifyingTypeInformation(MetaParameterizedType qualifyingTypeInformation);

  void addRegistrationHook(RegistrationHook registrationHook);
}
