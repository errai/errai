package org.jboss.errai.demo.grocery.client.shared;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
  
  private Set<Item> items = new HashSet<Item>();
  
  
}
