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
package org.jboss.errai.workspaces.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.LayoutPopupPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.Role;
import org.jboss.errai.bus.client.security.SecurityService;
import org.jboss.errai.workspaces.client.framework.Registry;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;

import java.util.Date;
import java.util.Set;


/**
 * Top header
 */
public class Header extends LayoutPanel
{
  private HTML username = new HTML("Unknown user");
  private Date loginDate;

  public Header()
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    //this.setStyleName("bpm-header");

    createInfoPanel();

    ErraiBus.get().subscribe("appContext.login", new MessageCallback()
    {      
      public void callback(Message message)
      {
        AuthenticationContext authContext =
            Registry.get(SecurityService.class).getAuthenticationContext();
        String userName = authContext.getName() != "" ?
            authContext.getName() : "Not authenticated";
        username.setText(userName);
        loginDate = new Date();
        layout();
      }
    });
  }
  
  private void createInfoPanel()
  {

    // ----------- logo panel
    LayoutPanel logoPanel = new LayoutPanel(new BoxLayout());
    logoPanel.setStyleName("bpm-header-left");

    /*Image logo = new Image(appContext.getConfig().getLogo());
    logo.setHeight("50");
    logoPanel.add(logo);*/

    // ----------- info panel
    HorizontalPanel infoPanel = new HorizontalPanel();
    infoPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
    infoPanel.setSpacing(5);
    infoPanel.setStyleName("bpm-header-right");

    // account info
    ErraiImageBundle icons = GWT.create(ErraiImageBundle.class);
    Image img = new Image(icons.user());
    img.addClickHandler(
        new ClickHandler()
        {

          public void onClick(ClickEvent clickEvent)
          {            
            String sessionId = Cookies.getCookie("JSESSIONID") != null ?
                            Cookies.getCookie("JSESSIONID") : "";
            AuthenticationContext authContext = Registry.get(SecurityService.class).getAuthenticationContext();
            Set<Role> roleSet = authContext.getRoles();

            StringBuffer roles = new StringBuffer();
            for(Role r : roleSet)
            {
              roles.append(r.getRoleName()).append(" ");
            }

            StringBuffer sb = new StringBuffer("<h3>User information</h3>");
            sb.append("- User: ").append(authContext.getName()).append("<br/>");
            sb.append("- Logged in since: ").append(loginDate).append("<br/>");
            sb.append("- SID: ").append(sessionId).append("<br/>");
            sb.append("- Roles: ").append(roles.toString()).append("<br/>");


            final LayoutPopupPanel popup = new LayoutPopupPanel(true);
            popup.setPopupPosition(
                clickEvent.getRelativeElement().getAbsoluteLeft()-120,
                clickEvent.getRelativeElement().getAbsoluteTop()+20
            );
            popup.setAnimationEnabled(true);
            popup.setSize("240px", "130px");
            HTML html = new HTML(sb.toString());
            html.setStyleName("bpm-user-info-popup");
            popup.add(html);
            popup.show();
          }
        }
    );


    Button btn = new Button("Logout", new ClickHandler()
    {

      public void onClick(ClickEvent clickEvent)
      {
        MessageBuilder.createMessage()
            .toSubject("AuthenticationService")
            .command(SecurityCommands.EndSession)
            .noErrorHandling().sendNowWith(ErraiBus.get());
      }
    }
    );
    
    infoPanel.add(img);
    infoPanel.add(username);
    infoPanel.add(btn);

    this.add(logoPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    this.add(infoPanel, new BoxLayoutData(177, 50));
  }
}
