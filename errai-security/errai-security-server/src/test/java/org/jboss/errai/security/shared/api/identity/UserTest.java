package org.jboss.errai.security.shared.api.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.marshalling.client.MarshallingSessionProviderFactory;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.shared.api.GroupImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.junit.Test;

public class UserTest {

  @Test
  public void userImplShouldCaptureRolesFromConstructor() {
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.getRoles().contains(new RoleImpl("a")));
  }
  
  @Test
  public void userImplShouldCaptureGroupsFromConstructor() {
    User user = new UserImpl("test", Collections.<Role> emptySet(), 
            Arrays.asList(new GroupImpl("a"), new GroupImpl("b"), new GroupImpl("c")));
    assertTrue(user.getGroups().contains(new GroupImpl("b")));
  }

  @Test
  public void testHasAnyRoles() {
    UserImpl user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.hasAnyRoles("a"));
    assertTrue(user.hasAnyRoles("b", "c"));
    assertTrue(user.hasAnyRoles("a", "f"));

    assertFalse(user.hasAnyRoles("f"));
    assertFalse(user.hasAnyRoles("f", "d"));
    assertFalse(user.hasAnyRoles());
  }

  @Test
  public void testHasAllRoles() throws Exception {
    UserImpl user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")));
    assertTrue(user.hasAllRoles("a"));
    assertTrue(user.hasAllRoles("a", "b"));
    assertTrue(user.hasAllRoles("c", "a", "b"));

    assertFalse(user.hasAllRoles("f"));
    assertFalse(user.hasAllRoles("a", "f"));
    assertFalse(user.hasAllRoles("a", "b", "f", "c"));
    assertTrue(user.hasAllRoles());
  }

  @Test
  public void testHasAnyGroups() {
    UserImpl user = new UserImpl("test", Collections.<Role> emptyList(), 
            Arrays.asList(new GroupImpl("a"), new GroupImpl("b"), new GroupImpl("c")));
    assertTrue(user.hasAnyGroups("a"));
    assertTrue(user.hasAnyGroups("b", "c"));
    assertTrue(user.hasAnyGroups("a", "f"));

    assertFalse(user.hasAnyGroups("f"));
    assertFalse(user.hasAnyGroups("f", "d"));
    assertFalse(user.hasAnyGroups());
  }

  @Test
  public void testHasAllGroups() throws Exception {
    UserImpl user = new UserImpl("test", Collections.<Role> emptyList(), 
            Arrays.asList(new GroupImpl("a"), new GroupImpl("b"), new GroupImpl("c")));
    assertTrue(user.hasAllGroups("a"));
    assertTrue(user.hasAllGroups("a", "b"));
    assertTrue(user.hasAllGroups("c", "a", "b"));

    assertFalse(user.hasAllGroups("f"));
    assertFalse(user.hasAllGroups("a", "f"));
    assertFalse(user.hasAllGroups("a", "b", "f", "c"));
    assertTrue(user.hasAllGroups());
  }

  @Test
  public void userShouldBePortable() {
    Map<String, String> randomProperties = new HashMap<String, String>();
    randomProperties.put("rand1", "RAND1");
    randomProperties.put("rand2", "RAND2");
    randomProperties.put("rand3", "RAND3");
    User user = new UserImpl("test", Arrays.asList(new RoleImpl("a"), new RoleImpl("b"), new RoleImpl("c")), randomProperties);

    if (!MarshallingSessionProviderFactory.isMarshallingSessionProviderRegistered()) {
      MappingContextSingleton.loadDynamicMarshallers();
    }

    String userAsJson = Marshalling.toJSON(user);
    User unmarshalledUser = (User) Marshalling.fromJSON(userAsJson);

    assertEquals(user.getIdentifier(), unmarshalledUser.getIdentifier());
    assertEquals(user.getRoles(), unmarshalledUser.getRoles());
    assertEquals(user.getProperties(), unmarshalledUser.getProperties());
  }
}
