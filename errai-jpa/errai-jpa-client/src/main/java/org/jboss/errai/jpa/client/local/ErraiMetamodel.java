package org.jboss.errai.jpa.client.local;

import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

/**
 * Trivial implementation of the JPA Metamodel class. This class is normally
 * instantiated and populated by generated code in ErraiEntityManager.
 * <p>
 * Instances of this class have two distinct lifecycle phases: when first
 * constructed, the package-private methods {@link #addEntityType(EntityType)}
 * and friends may be called to add new entity types. This is normally done from
 * generated code, but test code can also do this manually. Once
 * {@link #freeze()} has been called, the instance is "frozen." When frozen, all
 * the Metamodel interface methods operate properly, but the addXXX() methods
 * throw exceptions when called.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ErraiMetamodel implements Metamodel {

  // these are populated by a call to freeze()
  private ImmutableBiMap<Class<?>, EntityType<?>> entityTypes;
  private ImmutableBiMap<Class<?>, ManagedType<?>> managedTypes;
  private ImmutableBiMap<Class<?>, EmbeddableType<?>> embeddableTypes;

  // these are set to null when freeze() is called
  private Builder<Class<?>, EntityType<?>> entityTypeBuilder = ImmutableBiMap.builder();
  private Builder<Class<?>, ManagedType<?>> managedTypeBuilder = ImmutableBiMap.builder();
  private Builder<Class<?>, EmbeddableType<?>> embeddableTypeBuilder = ImmutableBiMap.builder();

  <X> void addEntityType(EntityType<X> e) {
    entityTypeBuilder.put(e.getJavaType(), e);
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  <X> void addManagedType(ManagedType<X> e) {
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  <X> void addEmbeddableType(EmbeddableType<X> e) {
    embeddableTypeBuilder.put(e.getJavaType(), e);
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  /**
   * Freezes the definition of this metamodel. Once frozen, no more entity
   * metadata can be added, and the collections returned by
   * {@link #getEntities()} and friends are immutable.
   */
  void freeze() {
    entityTypes = entityTypeBuilder.build();
    entityTypeBuilder = null;

    managedTypes = managedTypeBuilder.build();
    managedTypeBuilder = null;

    embeddableTypes = embeddableTypeBuilder.build();
    embeddableTypeBuilder = null;
  }

  /**
   * Returns true iff this instance is frozen. See the class-level documentation
   * for a description of the implications.
   */
  boolean isFrozen() {
    return entityTypeBuilder == null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X> ErraiEntityType<X> entity(Class<X> cls) {
    ErraiEntityType<X> et = (ErraiEntityType<X>) entityTypes.get(cls);
    if (et == null) {
      throw new IllegalArgumentException(cls.getName() + " is not a known entity type");
    }
    return et;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X> ManagedType<X> managedType(Class<X> cls) {
    return (ManagedType<X>) managedTypes.get(cls);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X> EmbeddableType<X> embeddable(Class<X> cls) {
    return (EmbeddableType<X>) embeddableTypes.get(cls);
  }

  @Override
  public Set<ManagedType<?>> getManagedTypes() {
    return managedTypes.values();
  }

  @Override
  public Set<EntityType<?>> getEntities() {
    return entityTypes.values();
  }

  @Override
  public Set<EmbeddableType<?>> getEmbeddables() {
    return embeddableTypes.values();
  }

}
