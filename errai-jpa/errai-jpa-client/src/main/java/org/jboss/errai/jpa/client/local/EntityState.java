package org.jboss.errai.jpa.client.local;

/**
 * Represents the states an entity instance can be in, according to the
 * ErraiEntityManager.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public enum EntityState {
  NEW,
  MANAGED,
  DETACHED,
  REMOVED
}
