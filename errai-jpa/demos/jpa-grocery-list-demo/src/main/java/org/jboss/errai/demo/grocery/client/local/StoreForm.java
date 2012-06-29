package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.demo.grocery.client.shared.Store;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;

@Templated
public class StoreForm extends Composite {

  @Inject private EntityManager em;

  private Store store;

  @Inject @DataField private TextBox name;
  @Inject @DataField private Button saveButton;

  public void setStore(Store store) {
    // TODO use data binding
    this.store = store;
    name.setText(store.getName());
  }

  @PostConstruct
  private void init() {
    setStore(new Store());

    // FEATURE
    saveButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        store.setName(name.getText());
        em.persist(store);
        em.flush();
        setStore(new Store());
      }
    });
  }
}
