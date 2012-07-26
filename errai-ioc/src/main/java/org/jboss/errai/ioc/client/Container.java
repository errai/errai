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

package org.jboss.errai.ioc.client;

import static org.jboss.errai.common.client.util.LogUtil.displayDebuggerUtilityTitle;
import static org.jboss.errai.common.client.util.LogUtil.displaySeparator;
import static org.jboss.errai.common.client.util.LogUtil.log;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.ioc.client.container.BeanRef;
import org.jboss.errai.ioc.client.container.IOCBeanManagerLifecycle;

import java.lang.annotation.Annotation;

public class Container implements EntryPoint {
  @Override
  public void onModuleLoad() {
    bootstrapContainer();
  }

  // stored for debugging purposes only. overwritten every time the container is bootstrapped.
  private static BootstrapperInjectionContext injectionContext;

  public void bootstrapContainer() {
    try {
      InitVotes.waitFor(Container.class);

      QualifierUtil.initFromFactoryProvider(new QualifierEqualityFactoryProvider() {
        @Override
        public QualifierEqualityFactory provide() {
          return GWT.create(QualifierEqualityFactory.class);
        }
      });

      new IOCBeanManagerLifecycle().resetBeanManager();

      final Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);

      log("IOC bootstrapper successfully initialized.");

      final BootstrapperInjectionContext ctx = bootstrapper.bootstrapContainer();
      log("IOC container bootstrapped.");

      ctx.getRootContext().finish();
      log(ctx.getRootContext().getAllCreatedBeans().size() + " beans successfully deployed.");

      InitVotes.voteFor(Container.class);

      injectionContext = ctx;

      declareDebugFunction();

    }
    catch (Throwable t) {
      t.printStackTrace();
      throw new RuntimeException("critical error in IOC container bootstrap", t);
    }
  }

  private static native void declareDebugFunction() /*-{
    $wnd.errai_bean_manager_status = function () {
      @org.jboss.errai.ioc.client.Container::displayBeanManagerStatus()();
    }
  }-*/;

  private static void displayBeanManagerStatus() {
    displayDebuggerUtilityTitle("BeanManager Status");

    log("[WIRED BEANS]");
    for (final BeanRef ref : injectionContext.getRootContext().getAllCreatedBeans()) {
      log(" -> " + ref.getClazz().getName());
      log("     qualifiers: " + annotationsToString(ref.getAnnotations()) + ")");
    }
    log("Total: " + injectionContext.getRootContext().getAllCreatedBeans().size());
    displaySeparator();
  }

  private static String annotationsToString(final Annotation[] annotations) {
    final StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < annotations.length; i++) {
      sb.append(annotations[i].annotationType().getName());

      if (i + 1 < annotations.length) sb.append(", ");
    }
    return sb.toString();
  }
}
