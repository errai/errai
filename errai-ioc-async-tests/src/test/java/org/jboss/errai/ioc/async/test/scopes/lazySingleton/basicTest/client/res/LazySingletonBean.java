package org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.LazySingleton;
import org.jboss.errai.ioc.client.api.LoadAsync;

import com.google.common.base.Preconditions;

@Singleton
@LazySingleton
@LoadAsync
public class LazySingletonBean {

  public static int instances;

  @Inject
  private SingletonBean singleton;

  private boolean postConstructed;
  @PostConstruct
  private void postConstr() {
    if(postConstructed)throw new IllegalArgumentException("already postconstructed");
    postConstructed=true;
    assert (SingletonBean.instances > instances) : "Singletonbean must be called before calling postConstruct";
    instances++;
  }
  
  public void doSomeTHing() {
    if(!postConstructed)throw new IllegalArgumentException("postConstruct not called yet");
  }
}
