package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Thrown to indicate an authentication attempt has failed.
 * <p>
 * TODO delete this and use the javax.security or org.picketlink type.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Portable
public class AuthenticationException extends RuntimeException {

}
