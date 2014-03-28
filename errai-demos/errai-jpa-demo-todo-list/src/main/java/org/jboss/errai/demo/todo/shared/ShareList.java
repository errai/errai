package org.jboss.errai.demo.todo.shared;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Entity
@NamedQueries({
  @NamedQuery(name = "sharedWithMe", query = "SELECT s.user FROM ShareList s, in (s.sharedWith) w WHERE w.loginName = :loginName"),
  @NamedQuery(name = "mySharedLists", query = "SELECT s FROM ShareList s WHERE s.user.loginName = :loginName")
})
public class ShareList {
  @Id
  @GeneratedValue
  private Long id;

  @OneToOne
  private TodoListUser user;

  @ManyToMany
  private List<TodoListUser> sharedWith;

  public Long getId() {
    return id;
  }

  public TodoListUser getUser() {
    return user;
  }

  public void setUser(TodoListUser user) {
    this.user = user;
  }

  public List<TodoListUser> getSharedWith() {
    if (sharedWith == null) {
      sharedWith = new ArrayList<TodoListUser>();
    }
    return sharedWith;
  }

  public void setSharedWith(List<TodoListUser> sharedWith) {
    this.sharedWith = sharedWith;
  }

  @Override
  public String toString() {
    return "ShareList[" +
            "id=" + id +
            ", user=" + user +
            ", sharedWith=" + sharedWith +
            ']';
  }
}
