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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import org.gwt.mosaic.ui.client.LayoutPopupPanel;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;
import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.client.protocols.SecurityCommands;
import org.jboss.errai.workspaces.client.icons.ErraiImageBundle;


/**
 * Top header
 */
public class Header extends LayoutPanel
{
  private Image loadingImage;
  
  private HTML username = new HTML("Unknown user");

  // avoid flickering image
  final Timer turnOffLoading = new Timer() {
    public void run() {
      //loadingImage.setVisible(false);
    }
  };

  public Header()
  {
    super(new BoxLayout(BoxLayout.Orientation.HORIZONTAL));
    this.setStyleName("bpm-header");

    createInfoPanel();

    ErraiBus.get().subscribe("appContext", new MessageCallback()
    {
      @Override
      public void callback(Message message)
      {
        username.setText( message.get(String.class, "username"));
        layout();
      }
    });
  }

  private void updateUser()
  {
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

    /*LayoutPanel loadingImageContainer = new LayoutPanel();
    loadingImageContainer.setStyleName("bpm-loading-image");

    loadingImage = new Image("images/ajax-loader.gif");
    loadingImageContainer.add(loadingImage);*/

    setLoading(false);

    // account info
    ErraiImageBundle icons = GWT.create(ErraiImageBundle.class);
    Image img = new Image(icons.user());
    img.addClickListener(
        new ClickListener()
        {

          public void onClick(Widget widget)
          {
            StringBuffer sb = new StringBuffer("<h3>User information</h3>");
            sb.append("- User: ").append(username.getText()).append("<br/>");
            sb.append("- Logged in since: ").append("").append("<br/>");
            sb.append("- SID: ").append("").append("<br/>");
            sb.append("- Roles: ").append("").append("<br/>");


            final LayoutPopupPanel popup = new LayoutPopupPanel(true);
            popup.setPopupPosition(
                widget.getAbsoluteLeft()-120,
                widget.getAbsoluteTop()+20
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


    Button btn = new Button("Logout", new ClickListener()
    {

      public void onClick(Widget widget)
      {
        // TODO: Optional service: send to appContext#logout instead 
         MessageBuilder.createMessage()
                      .toSubject("AuthorizationService")
                      .command(SecurityCommands.EndSession)
                      .noErrorHandling().sendNowWith(ErraiBus.get());
      }
    }
    );

    //infoPanel.add(loadingImageContainer);
    infoPanel.add(img);
    infoPanel.add(username);
    infoPanel.add(btn);

    this.add(logoPanel, new BoxLayoutData(BoxLayoutData.FillStyle.HORIZONTAL));
    this.add(infoPanel, new BoxLayoutData(177, 50));
  }

  public void setLoading(boolean doDisplay)
  {
    if(doDisplay)
      loadingImage.setVisible(doDisplay);
    else
      turnOffLoading.schedule(1000);
  }
}
