/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.widgets.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;
import org.gwt.mosaic.ui.client.layout.BoxLayout;
import org.gwt.mosaic.ui.client.layout.BoxLayoutData;
import org.gwt.mosaic.ui.client.layout.LayoutPanel;

import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.user.client.Event.*;


public class WSLaunchButton extends LayoutPanel {
  private static final String CSS_NAME = "WSLaunchButton";


  private String name;

  private List<ClickHandler> clickHandlers;

  public WSLaunchButton(ImageResource resource, String name) {
    super(new BoxLayout(BoxLayout.Orientation.VERTICAL));


    this.name = name;

    sinkEvents(Event.MOUSEEVENTS);

    HTML html = new HTML("&nbsp;&nbsp;" + AbstractImagePrototype.create(resource).getHTML() + "&nbsp;" + createButtonMarkup());
    this.add(html, new BoxLayoutData(BoxLayoutData.FillStyle.VERTICAL));
    this.setStylePrimaryName(CSS_NAME);

  }


  @Override
  public void onBrowserEvent(Event event) {
    if (!isAttached()) {
      return;
    }

    switch (event.getTypeInt()) {
      case ONMOUSEMOVE:
        break;
      case ONMOUSEOVER:
        addStyleDependentName("hover");
        break;
      case ONBLUR:
      case ONLOSECAPTURE:
      case ONMOUSEOUT:
        removeStyleDependentName("hover");
        removeStyleDependentName("down");
        break;
      case ONMOUSEDOWN:
        addStyleDependentName("down");
        break;
      case ONMOUSEUP:
        if (clickHandlers != null) {
          for (ClickHandler listen : clickHandlers) {
            listen.onClick(null);
          }
        }
        setStyleName(CSS_NAME);
        break;
    }

  }

  private String createButtonMarkup() {
    return "<span class=\"" + CSS_NAME + "-contents\"> " + name + "</span>";
  }

  public String getName() {
    return name;
  }

  public void addClickListener(ClickHandler handler) {
    if (clickHandlers == null) clickHandlers = new ArrayList<ClickHandler>();
    clickHandlers.add(handler);
  }
}
