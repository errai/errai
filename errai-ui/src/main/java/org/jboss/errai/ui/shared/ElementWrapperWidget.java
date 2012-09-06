/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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
package org.jboss.errai.ui.shared;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Used to merge a {@link Template} onto a {@link Composite} component.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class ElementWrapperWidget extends Widget implements HasHTML {

  private EventListener listener;

  public ElementWrapperWidget(Element wrapped) {
    if (wrapped == null) {
      throw new IllegalArgumentException(
              "Element to be wrapped must not be null - Did you forget to initialize or @Inject a @DataField?");
    }
    this.setElement(wrapped);
    DOM.setEventListener(this.getElement(), this);
  }
  
  @Override
  public String getText() {
    return getElement().getInnerText();
  }

  @Override
  public void setText(String text) {
    getElement().setInnerText(text);
  }

  @Override
  public String getHTML() {
    return getElement().getInnerHTML();
  }

  @Override
  public void setHTML(String html) {
    getElement().setInnerHTML(html);
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  public void setEventsToSink(int eventsToSink) {
    sinkEvents(eventsToSink);
  }

}
