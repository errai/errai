package org.jboss.errai.demo.grocery.client.local;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
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

@Dependent
@Templated
public class StoreForm extends Composite {

  @Inject private EntityManager em;

  // injecting this data binder causes automatic binding between
  // the properties of Store and the like-named @DataField members in this class
  // Example: property "store.name" tracks the value in the TextBox "name"
  @Inject private DataBinder<Store> storeBinder;

  @Inject @DataField private TextBox name;

  @Inject @DataField private Button saveButton;

  private Runnable afterSaveAction;

  public void setStore(Store store) {
    Assert.notNull(store);

    // TODO: do we need to unbind the existing model before adding the new one? (data binding javadoc)
    storeBinder.setModel(store, InitialState.FROM_MODEL);
  }

  @PostConstruct
  private void init() {

    // TODO (errai-ui): I want to get events with an annotated method (like GWT's @UiHandler)
    //                  rather than programmatically subscribing a listener
    saveButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {

        // XXX the following silliness will be unnecessary when Errai JPA knows about WrappedPortable
        Store s = (Store) ((WrappedPortable) storeBinder.getModel()).unwrap();

        em.persist(s);
        em.flush();

        if (afterSaveAction != null) {
          afterSaveAction.run();
        }

        setStore(new Store());
      }
    });
  }

  public void setAfterSaveAction(Runnable afterSaveAction) {
    this.afterSaveAction = afterSaveAction;
  }
}
