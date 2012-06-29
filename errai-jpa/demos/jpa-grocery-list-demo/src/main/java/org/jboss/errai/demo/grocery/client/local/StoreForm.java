package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.common.client.api.WrappedPortable;
import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
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

  // injecting this data binder causes automatic binding between
  // the String property "store.name" and the text box "name"
  @Inject private DataBinder<Store> storeBinder;

  @Inject @DataField private TextBox name;
  @Inject @DataField private Button saveButton;

  public void setStore(Store store) {
    Assert.notNull(store);
    // do we need to unbind the existing model before adding the new one?

    // FIXME data binder should be okay with null model properties
    if (store.getName() == null) store.setName("");

    storeBinder.setModel(store, InitialState.FROM_MODEL);
  }

  @PostConstruct
  private void init() {
    setStore(new Store());

    // FEATURE
    saveButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {

        // XXX the following silliness will be unnecessary when Errai JPA knows about WrappedPortable
        Store s = (Store) ((WrappedPortable) storeBinder.getModel()).unwrap();

        em.persist(s);
        em.flush();

        setStore(new Store());
      }
    });
  }
}
