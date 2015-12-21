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

import org.jboss.errai.common.client.api.Assert;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * A factory for creating {@link TransitionAnchor} instances.  This is
 * useful when, for example, showing a list of items that each are hyperlinked
 * to the same {@link Page} but with different {@link PageState}.
 *
 * @param <P> The type of the target page ("to page")
 * @author eric.wittmann@redhat.com
 */
public final class TransitionAnchorFactory<P> {

  private final Navigation navigation;
  private final Class<P> toPageWidgetType;
  private final HistoryTokenFactory htFactory;

  /**
   * Creates a new {@link TransitionAnchorFactory}.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  TransitionAnchorFactory(Navigation navigation, final Class<P> toPage, HistoryTokenFactory htFactory) {
    this.navigation = Assert.notNull(navigation);
    this.toPageWidgetType = Assert.notNull(toPage);
    this.htFactory = Assert.notNull(htFactory);
  }

  /**
   * Gets an instance of a {@link TransitionAnchor} without any additional
   * {@link PageState}.
   */
  public TransitionAnchor<P> get() {
    return new TransitionAnchor<P>(navigation, toPageWidgetType, htFactory);
  }

  /**
   * Gets an instance of a {@link TransitionAnchor} with the given {@link PageState}.
   * @param state
   */
  public TransitionAnchor<P> get(Multimap<String, String> state) {
    return new TransitionAnchor<P>(navigation, toPageWidgetType, state, htFactory);
  }

  /**
   * Gets an instance of a {@link TransitionAnchor} with the given single piece of
   * {@link PageState}.  This is a convenience for the use-case where the target
   * {@link Page} has a single piece of state, such as a UUID.
   * @param stateKey
   * @param stateValue
   */
  public TransitionAnchor<P> get(String stateKey, String stateValue) {
    Multimap<String, String> state = HashMultimap.create();
    state.put(stateKey, stateValue);
    return new TransitionAnchor<P>(navigation, toPageWidgetType, state, htFactory);
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPageType() {
    return toPageWidgetType;
  }

}
