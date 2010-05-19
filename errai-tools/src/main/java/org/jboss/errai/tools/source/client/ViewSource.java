/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.tools.source.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.gwt.mosaic.ui.client.LayoutPopupPanel;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 18, 2010
 */
public class ViewSource
{
  public static void on(final LayoutPanel widget, final String[] sourceNames)
  {
    Timer t = new Timer()
    {
      @Override
      public void run()
      {
        final int left = widget.getAbsoluteLeft();
        final int top = widget.getAbsoluteTop();
        final int width = widget.getOffsetWidth();
        final int height = widget.getOffsetHeight();

        final LayoutPopupPanel p = new LayoutPopupPanel(false);
        HTML html = new HTML("View Source");
        html.addClickHandler(new ClickHandler()
        {
          public void onClick(ClickEvent clickEvent)
          {
            LayoutPopupPanel p2 = new LayoutPopupPanel(true);
            p2.setModal(true);
            //p2.setAnimationEnabled(true);
            SourceViewWidget viewWidget = new SourceViewWidget(sourceNames);
            p2.setWidget(viewWidget);
            //p2.setPopupPosition(10, 10);

            int w2 = RootPanel.get().getOffsetWidth() - 100;
            int h2 = RootPanel.get().getOffsetHeight() - 100;
            p2.setSize(w2+"px", h2+"px");
            p2.center();

          }
        });
        p.setWidget(html);
        p.setPopupPosition(left+width-120, top+10);
        p.pack();
        p.show();

      }
    };

    t.schedule(1000);

  }
}
