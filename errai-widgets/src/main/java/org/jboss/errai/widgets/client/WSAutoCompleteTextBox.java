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

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

import java.util.ArrayList;


public class WSAutoCompleteTextBox extends TextBox
    implements ClickHandler, ChangeHandler, KeyUpHandler {

  private ListBox list = new ListBox();
  private PopupPanel popup = new PopupPanel(true);
  private String[] items = new String[]{};

  /**
   * Constructor. Sets up listeners and adds the list to the popup, and the popup to the main root panel.
   */
  public WSAutoCompleteTextBox() {
    super();
    addClickHandler(this);
    addKeyUpHandler(this);
    addChangeHandler(this);
    popup.add(list);
    //RootPanel.get().add(popup);
  }

  /**
   * Overrides onClick in ClickHandler. Completes selection.
   *
   * @param event the {@link ClickEvent} that was fired
   */
  public void onClick(ClickEvent event) {
    select();
  }

  /**
   * Overrides onChange in ChangeHandler. Completes the selection.
   *
   * @param event the {@link ChangeEvent} that was fired
   */
  public void onChange(ChangeEvent event) {
    select();
  }

  /**
   * Overrides onKeyUp in KeyUpHandler. Takes care of selection and showing popup box.
   * - If Enter is clicked, and the popup is showing: selection is made.
   * - Up/Down arrow keys are used to traverse through popup list.
   * - ESC key closes the popup list.
   * - Any other key prompts popup to show with appropriate items in the list.
   *
   * @param event the {@link KeyUpEvent} that was fired
   */
  public void onKeyUp(KeyUpEvent event) {
    if (event.isDownArrow()) {
      int selectedIndex = list.getSelectedIndex() + 1;
      if (selectedIndex > list.getItemCount())
        selectedIndex = 0;

      list.setSelectedIndex(selectedIndex);

    }
    else if (event.isUpArrow()) {
      int selectedIndex = list.getSelectedIndex() - 1;
      if (selectedIndex < 0)
        selectedIndex = list.getItemCount();

      list.setSelectedIndex(selectedIndex);

    }
    else if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      if (popup.isShowing())
        select();

    }
    else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
      cancel();

    }
    else {
      cancel();
      String text = getText();
      if (text.length() > 0) {
        String[] matches = getMatchingItems(text);
        if (matches.length > 0 && !(matches.length == 1 && matches[0].equals(text))) {

          for (int i = 0; i < matches.length; i++)
            list.addItem(matches[i]);
          list.setSelectedIndex(0);
          list.setVisibleItemCount(matches.length + 1);
          list.setWidth(getOffsetWidth() + "px");

          popup.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight());
          popup.show();
          popup.getElement().getStyle().setProperty("position", "absolute");
          popup.getElement().getStyle().setProperty("left", Integer.toString(getAbsoluteLeft()));
          popup.getElement().getStyle().setProperty("top", Integer.toString(getAbsoluteTop()
              + getOffsetHeight()));

        }
      }
    }
  }

  /**
   * Sets the applicable options.
   *
   * @param items the array of available options for the drop down list
   */
  public void setItems(String[] items) {
    this.items = items;
  }

  /**
   * Returns an array of items that start with the specified string.
   *
   * @param match the string to search for in the list of items
   * @return the string array of matching items; can be empty
   */
  private String[] getMatchingItems(String match) {
    ArrayList matches = new ArrayList();
    for (int i = 0; i < items.length; i++)
      if (items[i].toLowerCase().startsWith(match.toLowerCase()))
        matches.add(items[i]);

    return (String[]) matches.toArray(new String[matches.size()]);
  }

  /**
   * Completes the selection and cancels the popup.
   */
  private void select() {
    if (list.getItemCount() > 0)
      setText(list.getItemText(list.getSelectedIndex()));
    cancel();
  }

  /**
   * Cancels the popup.
   */
  private void cancel() {
    list.clear();
    popup.hide();
  }
}