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
  private User user;

  @ManyToMany
  private List<User> sharedWith;

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public List<User> getSharedWith() {
    if (sharedWith == null) {
      sharedWith = new ArrayList<User>();
    }
    return sharedWith;
  }

  public void setSharedWith(List<User> sharedWith) {
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
