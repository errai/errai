package org.jboss.errai.marshalling.tests.res;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Group {
  private int groupId;
  private String name;
  private List<User> usersInGroup;
  private Group subGroup;
  private Map<Group, User> groupUserMap;

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<User> getUsersInGroup() {
    return usersInGroup;
  }

  public void setUsersInGroup(List<User> usersInGroup) {
    this.usersInGroup = usersInGroup;
  }

  public Group getSubGroup() {
    return subGroup;
  }

  public void setSubGroup(Group subGroup) {
    this.subGroup = subGroup;
  }

  public Map<Group, User> getGroupUserMap() {
    return groupUserMap;
  }

  public void setGroupUserMap(Map<Group, User> groupUserMap) {
    this.groupUserMap = groupUserMap;
  }

  @Override
  public String toString() {
    return "Group:[groupId:" + groupId + ";name:" + name + ";usersInGroup:" + usersInGroup.size() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Group)) return false;

    Group group = (Group) o;

    if (groupId != group.groupId) return false;
    if (name != null ? !name.equals(group.name) : group.name != null) return false;
//        if (usersInGroup != null ? !usersInGroup.equals(group.usersInGroup) : group.usersInGroup != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = groupId;
    result = 31 * result + (name != null ? name.hashCode() : 0);
//        result = 31 * result + (usersInGroup != null ? usersInGroup.hashCode() : 0);
    return result;
  }
}
