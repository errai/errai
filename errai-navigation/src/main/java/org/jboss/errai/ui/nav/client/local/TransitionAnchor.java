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
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Anchor;

/**
 * Represents an anchor widget that, when clicked, will navigate the user
 * to another page in the application's flow.
 * <p>
 * Instances of this class are normally obtained via dependency injection.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <P> The type of the target page ("to page")
 * @author eric.wittmann@redhat.com
 */
public final class TransitionAnchor<P> extends Anchor implements ClickHandler {

  private final Navigation navigation;
  private final Class<P> toPageWidgetType;
  private final Multimap<String, String> state;
  private final HistoryTokenFactory htFactory;

  /**
   * Creates a new TransitionAnchor with the given attributes.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  TransitionAnchor(Navigation navigation, final Class<P> toPage, HistoryTokenFactory htFactory) {
    this(navigation, toPage, ImmutableMultimap.<String,String>of(), htFactory);
  }

  /**
   * Creates a new TransitionAnchor with the given attributes.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @param state
   *          The page state.  Cannot be null (but can be an empty multimap)
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  TransitionAnchor(Navigation navigation, final Class<P> toPage, final Multimap<String, String> state, HistoryTokenFactory htFactory) {
    this.navigation = Assert.notNull(navigation);
    this.toPageWidgetType = Assert.notNull(toPage);
    this.state = Assert.notNull(state);
    this.htFactory = Assert.notNull(htFactory);
    addClickHandler(this);
    addAttachHandler(new Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached())
          initHref(toPage, state);
      }
    });
  }

  /**
   * Initialize the anchor's href attribute.
   *
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @param state
   *          The page state.  Cannot be null (but can be an empty multimap)
   */
  private void initHref(Class<P> toPage, Multimap<String, String> state) {
    PageNode<P> toPageInstance = navigation.getNavGraph().getPage(toPage);
    HistoryToken token = htFactory.createHistoryToken(toPageInstance.name(), state);
    String href = "#" + token.toString();
    setHref(href);
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPageType() {
    return toPageWidgetType;
  }

  /**
   * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
   */
  @Override
  public void onClick(ClickEvent event) {
    if (isEnabled())
      navigation.goTo(toPageWidgetType, this.state);

    event.stopPropagation();
    event.preventDefault();
  }

  /**
   * Programmatically click on the anchor.
   */
  public void click() {
    navigation.goTo(toPageWidgetType, this.state);
  }

  /**
   * Programmatically click on the anchor (with alternate page state).
   * @param state
   */
  public void click(Multimap<String,String> state) {
    navigation.goTo(toPageWidgetType, state);
  }

}
