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

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.JsTypeProvider;

import com.google.gwt.core.client.js.JsType;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
@JsType
public class WindowInjectionContext {
  private Map<String, JsTypeProvider<?>> beanProviders = new HashMap<String, JsTypeProvider<?>>();

  public static WindowInjectionContext createOrGet() {
    if (!isWindowInjectionContextDefined()) {
      setWindowInjectionContext(new WindowInjectionContext());
    }
    return getWindowInjectionContext();
  }

  public static native WindowInjectionContext getWindowInjectionContext() /*-{
    return $wnd.injectionContext;
  }-*/;

  public static native void setWindowInjectionContext(WindowInjectionContext ic) /*-{
    $wnd.injectionContext = ic;
  }-*/;

  public static native boolean isWindowInjectionContextDefined() /*-{
    return !($wnd.injectionContext === undefined);
  }-*/;

  public void addBeanProvider(final String name, final JsTypeProvider<?> provider) {
    beanProviders.put(name, provider);
  }

  public void addSuperTypeAlias(final String superTypeName, final String typeName) {
    final JsTypeProvider<?> provider = beanProviders.get(typeName);

    if (provider != null) {
      beanProviders.put(superTypeName, provider);
    }
  }

  public Object getBean(final String name) {
    final JsTypeProvider<?> provider = beanProviders.get(name);

    if (provider == null) {
      throw new IOCResolutionException("no matching bean instances for: " + name);
    }

    return provider.getInstance();
  }

}
