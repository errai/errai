package org.jboss.errai.ui.nav.client.local.testpages;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.Label;

@Singleton @Page
public class SingletonScopedPage extends Label {

  private static int preDestroyCallCount = 0;

  @PreDestroy
  private void preDestroy() {
    preDestroyCallCount++;
  }

  public static int getPreDestroyCallCount() {
    return preDestroyCallCount;
  }

}
