package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ioc.client.LazySingleton;

import com.google.common.base.Preconditions;


@LazySingleton
@Singleton
public class SomeLazySingletonBeanForBeanManager {

  public static int instances;
  @Inject
  private SomeSingletonBean bean;
  
  @PostConstruct
  void onPostconstruct(){
    assert(SomeSingletonBean.instances>instances) : "SomeSingletonBean must be created before this lazy singleton bean";
    instances++;
  }
  
}
