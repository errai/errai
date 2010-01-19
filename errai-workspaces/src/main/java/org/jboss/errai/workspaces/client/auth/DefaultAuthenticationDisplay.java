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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.widgets.client.WSWindowPanel;
import org.jboss.errai.workspaces.client.widgets.WSLoginPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Default authentication form
 */
public class DefaultAuthenticationDisplay implements AuthenticationPresenter.Display,
    HasCloseHandlers<Object>
{
  private WSLoginPanel loginComponent = new WSLoginPanel();
  private WSWindowPanel loginWindowPanel;

  // requires adoption
  private List<CloseHandler> closeHandlers = new ArrayList<CloseHandler>();

  private Window.ClosingHandler loginWindowClosingHandler = new Window.ClosingHandler() {
    public void onWindowClosing(Window.ClosingEvent event) {

      // cheap delegation between WSWindowPanel and Presenter.Display
      for(CloseHandler handler : closeHandlers)
      {
        handler.onClose(null);
      }
    }
  };

  @Override
  public HandlerRegistration addCloseHandler(final CloseHandler handler)
  {
    this.closeHandlers.add(handler);
    return new HandlerRegistration()
    {
      @Override
      public void removeHandler()
      {
        closeHandlers.remove(handler);
      }
    };
  }

  @Override
  public void fireEvent(GwtEvent<?> gwtEvent)
  {

  }

  @Override
  public void clearPanel()
  {
    getUsernameInput().setText("");
    getPasswordInput().setText("");
  }

  @Override
  public HasText getUsernameInput()
  {
    return loginComponent.getUserNameInput();
  }

  @Override
  public HasText getPasswordInput()
  {
    return loginComponent.getPasswordInput();
  }

  @Override
  public HasClickHandlers getSubmitButton()
  {
    return loginComponent.getLoginButton();
  }

  @Override
  public HasCloseHandlers getWindowPanel()
  {
    return this;
  }

  public void hideLoginPanel() {
    if (loginWindowPanel != null) {
      loginWindowPanel.removeClosingHandler(loginWindowClosingHandler);
      loginWindowPanel.hide();
      RootPanel.get().remove(loginWindowPanel);
      loginWindowPanel = null;
    }
  }

  private void newWindowPanel() {
    hideLoginPanel();

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

  @Override
  public void showWelcomeMessage(String messageText)
  {
// display welcome panel
    final WSWindowPanel welcome = new WSWindowPanel();
    welcome.setWidth("250px");
    VerticalPanel vp = new VerticalPanel();
    vp.setWidth("100%");


    Label label = new Label(messageText);

    label.getElement().getStyle().setProperty("margin", "20px");

    vp.add(label);
    vp.setCellVerticalAlignment(label, HasAlignment.ALIGN_MIDDLE);
    vp.setCellHeight(label, "50px");

    Button okButton = new Button("OK");
    okButton.getElement().getStyle().setProperty("margin", "20px");

    vp.add(okButton);
    vp.setCellHorizontalAlignment(okButton, HasAlignment.ALIGN_CENTER);

    okButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        welcome.hide();
      }
    });

    welcome.add(vp);
    welcome.show();
    welcome.center();

    okButton.setFocus(true);

  }
}

