package org.jboss.errai.demo.todo.shared;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

@Portable @Bindable @Entity
@NamedQueries({
  @NamedQuery(name="currentItems", query="SELECT i FROM TodoItem i WHERE i.archived=false ORDER BY i.text"),
  @NamedQuery(name="allItemsForUser", query="SELECT i FROM TodoItem i WHERE i.user = :user ORDER BY i.text")
})
public class TodoItem {

  @Id @GeneratedValue
  private Long id;

  /**
   * The user who owns this To-do item.
   */
  @ManyToOne(cascade=CascadeType.MERGE)
  private User user;

  private String text;
  private Boolean done = Boolean.FALSE;
  private Boolean archived = Boolean.FALSE;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public User getUser() {
    return user;
  }
  public void setUser(User user) {
    this.user = user;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public Boolean isDone() {
    return done;
  }
  public void setDone(Boolean done) {
    this.done = done;
  }
  public Boolean isArchived() {
    return archived;
  }
  public void setArchived(Boolean archived) {
    this.archived = archived;
  }
  @Override
  public String toString() {
    return "TodoItem [id=" + id + ", user=" + (user == null ? "null" : user.getId()) + ", done=" + done +
            ", archived=" + archived + ", text=" + text + "]";
  }
}
