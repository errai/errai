/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.IOCProvider;

/**
 * {@link IOCProvider} to make the default navigation panel injectable.
 *
 * This is a contextual type provider so that it only matches NavigationPanel
 * exactly, and no supertypes.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@IOCProvider
@Singleton
public class NavigationPanelProvider implements Provider<NavigationPanel> {

  @Inject
  private Navigation navigation;

  @Override
  public NavigationPanel get() {
    if (!(navigation.getContentPanel() instanceof NavigationPanel)) {
      throw new RuntimeException("Default navigation panel is not of type: " + NavigationPanel.class.getName()
              + ". You replaced it with: " + navigation.getContentPanel().getClass().getName());
    }

    return (NavigationPanel) navigation.getContentPanel();
  }
}
