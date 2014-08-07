package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

@Singleton
public class SomeSingletonBean {

  public static int instances;
  
  private boolean postCOnstructed = false;
  
  @PostConstruct
  void onPostConstructed(){
    if(postCOnstructed)throw new IllegalStateException("already postconstructed");
    postCOnstructed=true;
    instances++;
  }
  
}
