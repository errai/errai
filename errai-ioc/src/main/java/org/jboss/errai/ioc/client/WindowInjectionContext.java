/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.ioc.client;

import java.util.List;

import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.JsTypeProvider;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import jsinterop.annotations.JsType;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType
public class WindowInjectionContext {
  private ListMultimap<String, JsTypeProvider<?>> beanProviders = ArrayListMultimap.create();

  public static WindowInjectionContext createOrGet() {
    if (!isWindowInjectionContextDefined()) {
      setWindowInjectionContext(new WindowInjectionContext());
    }
    return getWindowInjectionContext();
  }

  public static void reset() {
    setWindowInjectionContext(null);
  }

  public static native WindowInjectionContext getWindowInjectionContext() /*-{
    return $wnd.injectionContext;
  }-*/;

  public static native void setWindowInjectionContext(WindowInjectionContext ic) /*-{
    $wnd.injectionContext = ic;
  }-*/;

  public static native boolean isWindowInjectionContextDefined() /*-{
    return !($wnd.injectionContext === undefined || $wnd.injectionContext === null);
  }-*/;

  public void addBeanProvider(final String name, final JsTypeProvider<?> provider) {
    beanProviders.put(name, provider);
  }

  public void addSuperTypeAlias(final String superTypeName, final String typeName) {
    beanProviders.putAll(superTypeName, beanProviders.get(typeName));
  }

  public Object getBean(final String name) {
    final List<JsTypeProvider<?>> providers = beanProviders.get(name);

    if (providers.isEmpty()) {
      throw new IOCResolutionException("no matching bean instances for: " + name);
    }
    else if (providers.size() > 1) {
      throw new IOCResolutionException("multiple matching bean instances for: " + name);
    }
    else {
      return providers.get(0).getInstance();
    }
  }

  public JsArray<?> getBeans(final String name) {
    final List<JsTypeProvider<?>> providers = beanProviders.get(name);
    final Object[] retVal = new Object[providers.size()];

    for (int i = 0; i < providers.size(); i++) {
      retVal[i] = providers.get(i).getInstance();
    }

    return new JsArray<>(retVal);
  }

}
