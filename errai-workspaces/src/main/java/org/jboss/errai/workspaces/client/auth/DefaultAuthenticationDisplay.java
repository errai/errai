/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.errai.workspaces.client.auth;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.common.client.framework.WSComponent;
import org.jboss.errai.widgets.client.WSWindowPanel;
import org.jboss.errai.workspaces.client.widgets.WSLoginPanel;

import static org.jboss.errai.bus.client.MessageBuilder.createMessage;

/**
 * Default authentication form
 */
public class DefaultAuthenticationDisplay implements AuthenticationPresenter.Display
{
  private WSComponent loginComponent = new WSLoginPanel();
  private WSWindowPanel loginWindowPanel;

  private Window.ClosingHandler loginWindowClosingHandler = new Window.ClosingHandler() {
    public void onWindowClosing(Window.ClosingEvent event) {
      createMessage()
          .toSubject("ServerEchoService")
          .signalling()
          .noErrorHandling().sendNowWith(ErraiBus.get());
    }
  };


  @Override
  public void clearPanel()
  {
    //workspaceLayout.getUserInfoPanel().clear();
  }

  public void closeLoginPanel() {
    if (loginWindowPanel != null) {
      loginWindowPanel.removeClosingHandler(loginWindowClosingHandler);
      loginWindowPanel.hide();
      RootPanel.get().remove(loginWindowPanel);
      loginWindowPanel = null;
    }
  }

  private void newWindowPanel() {
    closeLoginPanel();

    loginWindowPanel = new WSWindowPanel();
    loginWindowPanel.addClosingHandler(loginWindowClosingHandler);
  }

  public void showLoginPanel() {
    newWindowPanel();
    loginWindowPanel.setTitle("Security Challenge");
    loginWindowPanel.add(loginComponent.getWidget());
    loginWindowPanel.showModal();
    loginWindowPanel.center();
  }
}

