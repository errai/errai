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

package org.jboss.errai.ui.nav.client.local;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.ContextualTypeProvider;
import org.jboss.errai.ioc.client.api.IOCProvider;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Provides new instances of the {@link TransitionAnchorFactory} class, which
 * allows them to be injected.
 * @author eric.wittmann@redhat.com
 */
@IOCProvider @Singleton
public class TransitionAnchorFactoryProvider implements ContextualTypeProvider<TransitionAnchorFactory<?>> {

  @Inject Navigation navigation;
  @Inject HistoryTokenFactory htFactory;

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public TransitionAnchorFactory provide(Class<?>[] typeargs, Annotation[] qualifiers) {
    Class<IsWidget> toPageType = (Class<IsWidget>) typeargs[0];
    return new TransitionAnchorFactory<IsWidget>(navigation, toPageType, htFactory);
  }

}
