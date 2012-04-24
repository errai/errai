package org.jboss.errai.cdi.injection.client.mvp;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * @author Mike Brock
 */
public class ContactsView implements ContactsPresenter.Display {
  @Override
  public HasClickHandlers getAddButton() {
    return null;
  }

  @Override
  public HasClickHandlers getDeleteButton() {
    return null;
  }

  @Override
  public HasClickHandlers getList() {
    return null;
  }

  @Override
  public void setData(List<String> data) {
  }

  @Override
  public int getClickedRow(ClickEvent event) {
    return 0;
  }

  @Override
  public List<Integer> getSelectedRows() {
    return null;
  }

  @Override
  public Widget asWidget() {
    return null;
  }
}
