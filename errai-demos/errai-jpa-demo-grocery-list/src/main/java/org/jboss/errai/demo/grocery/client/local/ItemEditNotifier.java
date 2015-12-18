package org.jboss.errai.demo.grocery.client.local;

/**
 * Event class to signify that an item is being edited
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class ItemEditNotifier {
  
  public static final int EDIT_EVENT = 1;
  public static final int DELETE_EVENT = 2;
  
  private int creatorId;
  private int notifierType;
  
  public ItemEditNotifier(int id, int eventType) {
    super();
      this.creatorId = id;
      this.setNotifierType(eventType);
  }
  
  public int getCreatorId() {
    return this.creatorId;
  }
  
  public void setCreatorId(int id) {
    this.creatorId = id;
  }
  
  public int getNotifierType() {
    return notifierType;
  }
  public void setNotifierType(int notifierType) {
    this.notifierType = notifierType;
  }
  
  public boolean isCreatedBy(int id) {
    if (id == this.getCreatorId()) {
      return true;
    }
    
    return false;
  }
}
