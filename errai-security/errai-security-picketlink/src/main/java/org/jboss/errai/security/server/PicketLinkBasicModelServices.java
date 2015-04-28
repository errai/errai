package org.jboss.errai.security.server;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;

public interface PicketLinkBasicModelServices {

  /**
   * <p>
   * Returns an {@link Role} instance with the given <code>name</code>.
   * </p>
   *
   * @param name
   *          The role's name.
   *
   * @return An {@link Role} instance or null if the <code>name</code> is null
   *         or an empty string.
   *
   * @throws IdentityManagementException
   *           If the method fails.
   */
  Role getRole(String name) throws IdentityManagementException;

  /**
   * <p>
   * Checks if the given {@link Role} is granted to the provided
   * {@link IdentityType}.
   * </p>
   *
   * @param assignee
   *          A previously loaded {@link IdentityType} instance. Valid instances
   *          are only from the {@link Account} and {@link Group} types.
   * @param role
   *          A previously loaded {@link Role} instance.
   *
   * @return True if the give {@link Role} is granted. Otherwise this method
   *         returns false.
   *
   * @throws IdentityManagementException
   *           If the method fails.
   */
  boolean hasRole(IdentityType assignee, Role role) throws IdentityManagementException;

  /**
   * <p>
   * Checks if the given {@link Role} is granted to the provided
   * {@link IdentityType}.
   * </p>
   *
   * @param assignee
   *          A previously loaded {@link IdentityType} instance. Valid instances
   *          are only from the {@link Account} and {@link Group} types.
   * @param role
   *          A previously loaded {@link Role} instance.
   *
   * @return True if the give {@link Role} is granted. Otherwise this method
   *         returns false.
   *
   * @throws IdentityManagementException
   *           If the method fails.
   */
  boolean hasRole(IdentityType assignee, String roleName) throws IdentityManagementException;
}
