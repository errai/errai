package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
@ExposeEntity
public class User {
    private int id;
    private String name;
    private List<Group> groups;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (groups != null ? !groups.equals(user.groups) : user.groups != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        return result;
    }

    public static User create() {
        User user = new User();
        user.id = 1;
        user.name = "mrfoo";

        user.groups = new ArrayList<Group>();

        Group adminGroup = new Group();
        adminGroup.setGroupId(1);
        adminGroup.setName("Admin");
        adminGroup.setUsersInGroup(new ArrayList<User>());

        user.groups.add(adminGroup);
        adminGroup.getUsersInGroup().add(user);

        return user;
    }
}
