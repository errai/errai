package org.jboss.errai.jpa.client.local;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.marshalling.client.Marshalling;

/**
 * Holder class for a storage key: a tuple of type and identity value.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <X> The entity's Java type
 * @param <T> The entity's identity type
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Key<X, T> {

  private final ErraiEntityType<X> entityType;
  private final T id;

  /**
   * Creates a key that uniquely identifies an entity of a particular type (the
   * entity with this identity does not necessarily exist, but if it did, this
   * would be its identity).
   *
   * @param entityType The type of the entity. Must not be null.
   * @param id The ID of the entity. Must not be null.
   * @throws NullPointerException if either argument is null.
   */
  public Key(ErraiEntityType<X> entityType, T id) {
    this.entityType = Assert.notNull(entityType);
    this.id = Assert.notNull(id);
  }

  /**
   * Returns a Key instance for the entity type of the given class.
   *
   * @param em
   *          The entity manager (required for looking up the EntityType for the
   *          given class). Must not be null.
   * @param entityClass
   *          The class of the entity for the key. Must not be null.
   * @param primaryKey
   *          The ID value for the entity. Must not be null.
   * @return A Key instance for the given entity type and ID value.
   * @throws NullPointerException
   *           if any argument is null.
   * @throws IllegalArgumentException
   *           if {@code entityClass} is not a known JPA entity type.
   */
  public static <X, T> Key<X, T> get(ErraiEntityManager em, Class<X> entityClass, T id) {
    ErraiEntityType<X> entityType = em.getMetamodel().entity(entityClass);
    return new Key<X, T>(entityType, id);
  }

  /**
   * Returns the entity type for this key.
   *
   * @return the entity type of the key. Never null.
   */
  public ErraiEntityType<X> getEntityType() {
    return entityType;
  }

  /**
   * Returns the ID value for this key.
   *
   * @return the ID value of the key. Never null.
   */
  public T getId() {
    return id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((entityType == null) ? 0 : entityType.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Key<?, ?> other = (Key<?, ?>) obj;
    if (entityType == null) {
      if (other.entityType != null)
        return false;
    }
    else if (!entityType.equals(other.entityType))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Key [entityType=" + entityType + ", id=" + id + "]";
  }

  public String toJson() {
    return "{ entityType: \"" + entityType.getJavaType().getName()
            + "\", id: " + Marshalling.toJSON(id) + "}";
  }
}
