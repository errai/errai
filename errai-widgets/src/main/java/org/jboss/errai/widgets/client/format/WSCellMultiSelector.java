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

package org.jboss.errai.widgets.client.format;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.widgets.client.WSGrid;

import java.util.LinkedHashSet;
import java.util.Set;

public class WSCellMultiSelector extends WSCellFormatter<String> {
  private Set<String> values;
  //  private HTML selection;
  private boolean updateble = true;

  private static ListBox listBox;
  private static WSCellMultiSelector editCellReference;

  static {
    listBox = new ListBox(false);
    listBox.setVisible(false);
    listBox.getElement().getStyle().setProperty("position", "absolute");

    RootPanel.get().add(listBox);

    listBox.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        int selIndex = listBox.getSelectedIndex();
        editCellReference.setValue(listBox.getItemText(selIndex));
        editCellReference.stopedit();
      }
    });
  }

  public WSCellMultiSelector(Set<String> values) {
    this(values, "");
  }

  public WSCellMultiSelector(Set<String> values, String defaultSelection) {
    this.values = new LinkedHashSet<String>();
    this.html = new HTML();
    if (defaultSelection != null) {
      this.values.add(defaultSelection);
      this.html.setHTML(defaultSelection);
    }
    this.values.addAll(values);

  }

  public void setValue(String value) {
    if (values.contains(value)) {
      super.setValue(value);
    }
    else if (updateble) {
      super.setValue(value);
      values.add(value);
    }
  }

  @Override
  public String getValue() {
    return html.getText();
  }

  public boolean edit(WSGrid.WSCell element) {
    wsCellReference = element;
    editCellReference = this;

    Style s = listBox.getElement().getStyle();

    s.setProperty("left", element.getAbsoluteLeft() + "px");
    s.setProperty("top", element.getAbsoluteTop() + "px");

    listBox.clear();

    int i = 0;
    for (String v : values) {
      listBox.addItem(v);
      if (html.getHTML().equals(v)) listBox.setSelectedIndex(i);
      i++;
    }

    listBox.setVisible(true);
    listBox.setFocus(true);
    return true;
  }

  public void stopedit() {
    listBox.setVisible(false);
    wsCellReference.stopedit();
  }
}
