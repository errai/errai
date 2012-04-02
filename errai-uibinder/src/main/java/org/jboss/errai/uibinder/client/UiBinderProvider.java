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

package org.jboss.errai.uibinder.client;

import com.google.gwt.uibinder.client.UiBinder;
import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
@IOCProvider @Singleton
public class UiBinderProvider implements ContextualTypeProvider<UiBinder<?, ?>> {
  private static final Map<Class<?>, UiBinder> UI_BINDER_MAP = new HashMap<Class<?>, UiBinder>();

  @Override
  public UiBinder provide(final Class<?>[] typeargs, Annotation[] qualifiers) {
    if (UI_BINDER_MAP.containsKey(typeargs[1])) {
      return UI_BINDER_MAP.get(typeargs[1]);
    }
    else {
      throw new RuntimeException("could not find UiBinder for: " + typeargs[1]);
    }
  }

  public static void registerBinder(Class<?> type, UiBinder binder) {
    UI_BINDER_MAP.put(type, binder);
  }
}
