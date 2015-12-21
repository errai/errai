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

package org.jboss.errai.ui.nav.client.local.testpages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;

import com.google.common.collect.HashMultimap;
import com.google.gwt.user.client.ui.FlowPanel;

@Dependent
@Page
public class PageWithTransitionAnchor extends FlowPanel {

  @Inject
  public TransitionAnchor<PageB> linkToB;

  @Inject
  public TransitionAnchorFactory<PageBWithState> linkFactory;

  @Inject
  public TransitionAnchor<NonCompositePage> linkToNonComp;

  @Inject
  public TransitionAnchorFactory<NonCompositePage> nonCompLinkFactory;

  /**
   * Constructor.
   */
  public PageWithTransitionAnchor() {
  }

  @PostConstruct
  protected void postCtor() {
    add(linkToB);
    add(linkFactory.get());
    add(linkFactory.get("uuid", "12345"));
    HashMultimap<String, String> state = HashMultimap.create();
    state.put("uuid", "54321");
    add(linkFactory.get(state));
    add(linkToNonComp);
  }

}
