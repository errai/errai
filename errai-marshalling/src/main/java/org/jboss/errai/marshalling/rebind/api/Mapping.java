package org.jboss.errai.marshalling.rebind.api;

import org.jboss.errai.codegen.framework.Statement;

/**
 * Classes which implement this interface should return valid statements that know how to bind/read a value
 * from a class.
 *
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Mapping {
  Statement getValueStatement();
}
