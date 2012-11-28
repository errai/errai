package org.jboss.errai.demo.grocery.client.shared;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 * Represents a grocery list: a collection of {@link Item grocery items} that is
 * maintained by a particular user.
 *
 * @author jfuerth
 */
@Entity
@NamedQuery(name="groceryListsForUser", query="SELECT gl FROM GroceryList gl WHERE gl.owner=:user")
public class GroceryList {

  @Id @GeneratedValue
  private long id;

  @ManyToOne
  private User owner;

  @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
  private List<Item> items = new ArrayList<Item>();

  public long getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public List<Item> getItems() {
    return items;
  }
}
