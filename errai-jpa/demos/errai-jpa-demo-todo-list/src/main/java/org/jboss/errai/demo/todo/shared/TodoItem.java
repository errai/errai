package org.jboss.errai.demo.todo.shared;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.jboss.errai.databinding.client.api.Bindable;

@Bindable @Entity
@NamedQueries({
  @NamedQuery(name="currentItems", query="SELECT i FROM TodoItem i WHERE i.archived=false ORDER BY i.text"),
  @NamedQuery(name="allItems", query="SELECT i FROM TodoItem i ORDER BY i.text")
})
public class TodoItem {

  @Id @GeneratedValue
  private long id;

  private String text;
  private boolean done;
  private boolean archived;

  public long getId() {
    return id;
  }
  public void setId(long id) {
    this.id = id;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public boolean isDone() {
    return done;
  }
  public void setDone(boolean done) {
    this.done = done;
  }
  public boolean isArchived() {
    return archived;
  }
  public void setArchived(boolean archived) {
    this.archived = archived;
  }
}
