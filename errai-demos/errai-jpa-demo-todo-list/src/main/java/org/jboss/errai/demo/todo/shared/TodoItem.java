package org.jboss.errai.demo.todo.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

@Portable @Bindable @Entity
@NamedQueries({
  @NamedQuery(name="currentItemsForUser", query="SELECT i FROM TodoItem i WHERE i.loginName = :userId AND i.archived=false ORDER BY i.text"),
  @NamedQuery(name = "allItemsForUser", query = "SELECT i FROM TodoItem i WHERE i.loginName = :userId ORDER BY i.text"),
  @NamedQuery(name = "allSharedItems", query = "SELECT i FROM TodoItem i WHERE i.loginName in :userIds ORDER BY i.loginName")
})
public class TodoItem {

  @Id @GeneratedValue
  private Long id;

  private String loginName;

  private String text;

  private Boolean done = Boolean.FALSE;
  private Boolean archived = Boolean.FALSE;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
  }
  public String getLoginName() {
    return loginName;
  }
  public void setLoginName(String loginName) {
    this.loginName = loginName;
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
    return "TodoItem [id=" + id + ", user=" + loginName + ", done=" + done +
            ", archived=" + archived + ", text=" + text + "]";
  }
}
