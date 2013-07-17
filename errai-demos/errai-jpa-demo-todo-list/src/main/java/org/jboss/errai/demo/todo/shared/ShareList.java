package org.jboss.errai.demo.todo.shared;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
@Entity
@NamedQuery(name="sharedWithMe", query="SELECT s.loginName FROM ShareList s, in (s.sharedWith) w WHERE w.loginName = :loginName")
public class ShareList {
  @Id
  private String loginName;

  @OneToMany
  private List<User> sharedWith;

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
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
            "loginName='" + loginName + '\'' +
            ", sharedWith=" + sharedWith +
            ']';
  }
}
