package org.jboss.errai.demo.todo.client.local.shadow;

import org.jboss.errai.demo.todo.shared.TodoListUser;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;

/**
 * A temporary user object that gets stored on the client when the signup failed because of network disconnect
 * @author edewit@redhat.com
 */
@Entity
@NamedQuery(name = "allTempUsers", query = "SELECT u FROM TempUser u")
public class TempUser {
  @Id
  private String email;
  private String password;
  private String fullName;
  private String shortName;
  private String loginName;

  public TempUser() {
  }

  public TempUser(TodoListUser user, String password) {
    this.email = user.getEmail();
    this.fullName = user.getFullName();
    this.shortName = user.getShortName();
    this.loginName = user.getLoginName();
    this.password = password;
  }

  public TodoListUser asUser() {
    TodoListUser user = new TodoListUser();
    user.setEmail(email);
    user.setFullName(fullName);
    user.setShortName(shortName);
    user.setLoginName(loginName);
    return user;
  }

  public String getPassword() {
    return password;
  }
}
