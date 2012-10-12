package org.jboss.errai.demo.grocery.client.local;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.demo.grocery.client.shared.Department;
import org.jboss.errai.demo.grocery.client.shared.Item;
import org.jboss.errai.demo.grocery.client.shared.User;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A form for editing the properties of a new or existing Item object.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
@Dependent
@Templated
public class ItemForm extends Composite {

  @Inject private EntityManager em;
  @Inject private User user;

  @Inject private Event<Item> newItemEvent;
  
  // injecting this data binder causes automatic binding between
  // the properties of Item and the like-named @DataField members in this class
  // Example: property "item.name" tracks the value in the TextBox "name"
  @Inject @AutoBound private DataBinder<Item> itemBinder;

  @Inject @Bound @DataField private SuggestBox name;
  @Inject @Bound @DataField private TextBox comment;

  /*
   * Not @Bound because the department name belongs to the nested Department
   * object, not the Item.
   */
  @Inject @DataField private SuggestBox department;

  @Inject @DataField private Button saveButton;

  private Runnable afterSaveAction;

  @PostConstruct
  private void setupSuggestions() {
    MultiWordSuggestOracle iso = (MultiWordSuggestOracle) name.getSuggestOracle();
    for (Item i : em.createNamedQuery("allItemsByName", Item.class).getResultList()) {
      iso.add(i.getName());
    }
    
    MultiWordSuggestOracle dso = (MultiWordSuggestOracle) department.getSuggestOracle();
    for (Department d : em.createNamedQuery("allDepartments", Department.class).getResultList()) {
      dso.add(d.getName());
    }
  }

  @SuppressWarnings("unused")
  private void onNewItem(@Observes Item newItem) {
    System.out.println("ItemForm@" + System.identityHashCode(this) + " got new item event");
    ((MultiWordSuggestOracle) name.getSuggestOracle()).add(newItem.getName());
  }
  
  /**
   * Returns the store instance that is permanently associated with this form.
   * The returned instance is bound to this store's fields: updates to the form
   * fields will cause matching updates in the returned object's state, and
   * vice-versa.
   *
   * @return the Item instance that is bound to the fields of this form.
   */
  public Item getItem() {
    return itemBinder.getModel();
  }

  public void setItem(Item item) {
    if (item.getDepartment() == null) {
      item.setDepartment(new Department());
    }
    department.setText(item.getDepartment().getName());
    itemBinder.setModel(item, InitialState.FROM_MODEL);
  }

  /**
   * Gives keyboard focus to the appropriate widget in this form.
   */
  public void grabKeyboardFocus() {
    name.setFocus(true);
  }

  // TODO (after ERRAI-366): make this method package-private
  @EventHandler("saveButton")
  public void onSaveButtonClicked(ClickEvent event) {
    TypedQuery<Department> deptQuery = em.createNamedQuery("departmentByName", Department.class);
    deptQuery.setParameter("name", department.getText());
    Department resolvedDepartment;
    List<Department> resultList = deptQuery.getResultList();
    if (resultList.isEmpty()) {
      resolvedDepartment = new Department();
      resolvedDepartment.setName(department.getText());
    }
    else {
      resolvedDepartment = resultList.get(0);
    }
    itemBinder.getModel().setDepartment(resolvedDepartment);

    itemBinder.getModel().setAddedBy(user);
    itemBinder.getModel().setAddedOn(new Date());

    em.persist(itemBinder.getModel());
    em.flush();

    
    if (afterSaveAction != null) {
      afterSaveAction.run();
    }
  }

  @PreDestroy
  void cleanup() {
    itemBinder.unbind();
  }

  public void setAfterSaveAction(Runnable afterSaveAction) {
    this.afterSaveAction = afterSaveAction;
  }

}
