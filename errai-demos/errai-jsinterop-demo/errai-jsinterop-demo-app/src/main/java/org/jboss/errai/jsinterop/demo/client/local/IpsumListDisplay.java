/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.jsinterop.demo.client.local;

import static com.google.gwt.user.client.Event.ONCLICK;
import static org.jboss.errai.common.client.dom.DOMUtil.removeAllChildren;
import static org.jboss.errai.common.client.dom.DOMUtil.removeAllElementChildren;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.NumberInput;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.Event;

@Dependent
@Templated
public class IpsumListDisplay implements IsElement {

  @Inject
  @AutoBound
  private DataBinder<List<IpsumDescriptor>> binder;

  @Inject
  @Bound
  @DataField
  @ListContainer("tbody")
  private ListComponent<IpsumDescriptor, IpsumDescriptorDisplay> list;

  @Inject
  @DataField
  private Div output;

  @Inject
  @DataField("para-number")
  private NumberInput number;

  @Inject
  private IpsumService ipsumService;

  @Inject
  private Document doc;

  @PostConstruct
  private void init() {
    removeAllElementChildren(list.getElement());
    list.setSelector(display -> display.getElement().getClassList().add("selected"));
    list.setDeselector(display -> display.getElement().getClassList().remove("selected"));
    binder.setModel(new ArrayList<>(ipsumService.getDescriptors()));
  }

  public void ipsumSelected(final @Observes IpsumDescriptor descriptor) {
    list.deselectAll();
    list.selectModel(descriptor);
  }

  @SinkNative(ONCLICK)
  @EventHandler("generate")
  private void generate(final Event evt) {
    list
      .getSelectedModels()
      .stream()
      .findFirst()
      .ifPresent(descriptor -> generateIpsum(descriptor));
  }

  private void generateIpsum(final IpsumDescriptor descriptor) {
    final int paragraphNumber = getParagraphNumber();
    ipsumService
      .lookup(descriptor)
      .map(generator -> generator.generateIpsum(paragraphNumber))
      .ifPresent(paragraphs -> displayParagraphs(paragraphs));
  }

  private void displayParagraphs(final String[] paragraphs) {
    removeAllChildren(output);
    Arrays
      .stream(paragraphs)
      .map(paragraph -> {
        final HTMLElement p = doc.createElement("p");
        p.setTextContent(paragraph);
        return p;
      })
      .forEachOrdered(p -> output.appendChild(p));
  }

  private int getParagraphNumber() {
    try {
      return Integer.parseInt(number.getValue());
    } catch (final NumberFormatException nfe) {
      number.setValue("5");
      return 5;
    }
  }

}
