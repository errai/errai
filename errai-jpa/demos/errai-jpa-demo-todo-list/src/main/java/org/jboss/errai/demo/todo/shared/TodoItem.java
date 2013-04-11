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
  @NamedQuery(name="currentItems", query="SELECT i FROM TodoItem i WHERE i.archived=false ORDER BY i.text"),
  @NamedQuery(name="allItems", query="SELECT i FROM TodoItem i ORDER BY i.text")
})
public class  TodoItem {

  @Id @GeneratedValue
  private Long id;

  private String text;
  private Boolean done = Boolean.FALSE;
  private Boolean archived = Boolean.FALSE;

  public Long getId() {
    return id;
  }
  public void setId(Long id) {
    this.id = id;
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
}
