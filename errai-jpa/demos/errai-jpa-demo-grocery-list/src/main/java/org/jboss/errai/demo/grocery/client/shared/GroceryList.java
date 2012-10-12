package org.jboss.errai.demo.grocery.client.shared;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Represents a grocery list: a collection of {@link Item grocery items} that is
 * maintained by a particular user.
 * 
 * @author jfuerth
 */
@Entity
public class GroceryList {

  @Id @GeneratedValue
  private long id;
  
  @ManyToOne
  private User owner;
  
  @OneToMany
  private List<Item> items = new ArrayList<Item>();
  
  public long getId() {
    return id;
  }
  
  public User getOwner() {
    return owner;
  }
  
  public List<Item> getItems() {
    return items;
  }
}
