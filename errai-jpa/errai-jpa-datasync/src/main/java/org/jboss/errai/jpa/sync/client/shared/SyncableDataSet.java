package org.jboss.errai.jpa.sync.client.shared;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.errai.common.client.api.Assert;

public class SyncableDataSet<E> {

  private final TypedQuery<E> query;
  private final EntityManager em;

  protected SyncableDataSet(EntityManager em, TypedQuery<E> query) {
    this.em = Assert.notNull(em);
    this.query = Assert.notNull(query);
  }

  public static <E> SyncableDataSet<E> from(EntityManager em, TypedQuery<E> query) {
    return new SyncableDataSet<E>(em, query);
  }

  public List<SyncResponse<E>> coldSync(List<SyncRequestOperation<E>> remoteResults) {
    Map<Object, E> localResults = new HashMap<Object, E>();
    for (E localEntity : query.getResultList()) {
      localResults.put(id(localEntity), localEntity);
    }

    // maps the old remote ID -> new local persistent entity
    Map<Object, E> newLocalEntities = new HashMap<Object, E>();

    // the response we will return
    List<SyncResponse<E>> syncResponse = new ArrayList<SyncResponse<E>>();

    for (SyncRequestOperation<E> syncReq : remoteResults) {
      final E remoteCopy = syncReq.getEntity();
      Object remoteId = id(remoteCopy);
      final E localCopy = localResults.get(remoteId);

      // TODO handle related entities reachable from the given ones

      switch (syncReq.getType()) {
      case EXISTING:
        localResults.remove(remoteId);
        E expectedLocalState = syncReq.getExpectedState();
        if (isDifferent(localCopy, expectedLocalState)) {
          syncResponse.add(new ConflictResponse<E>(expectedLocalState, localCopy, remoteCopy));
        }
        else {
          em.merge(remoteCopy);
          // don't need to generate a response here; we've accepted the merge
        }
        break;

      case NEW:
        clearId(remoteCopy);
        em.persist(remoteCopy);
        newLocalEntities.put(remoteId, remoteCopy);
      }
    }

    em.flush();

    // pick up new IDs (this has to be done after the flush)
    for (Map.Entry<Object, E> newLocalEntity : newLocalEntities.entrySet()) {
      syncResponse.add(new IdChangeResponse<E>(newLocalEntity.getKey(), newLocalEntity.getValue()));
    }

    for (E newOnThisSide : localResults.values()) {
      syncResponse.add(new NewRemoteEntityResponse<E>(newOnThisSide));
    }
    return syncResponse;
  }

  /**
   * Compares two JPA Managed Type instances to see if they are the same.
   *
   * @param lhs
   * @param rhs
   * @return
   */
  private <X> boolean isDifferent(X lhs, X rhs) {
    return isDifferent(lhs, rhs, new IdentityHashMap<Object, Object>());
  }

  /**
   * Private recursive subroutine of {@link #isDifferent(Object, Object)}.
   *
   * @param lhs
   * @param rhs
   * @param encountered
   * @return
   */
  private <X> boolean isDifferent(X lhs, X rhs, IdentityHashMap<Object, Object> encountered) {
    if (lhs == null && rhs == null) return false;
    if (lhs == null || rhs == null) return true;

    if (encountered.get(lhs) == rhs) {
      // we're already in the middle of comparing lhs to rhs, so pretend they're equal for now.
      // if they're not really equal, the truth will come out once the stack has unwound.
      return false;
    }

    encountered.put(lhs, rhs);

    // XXX probably need to pass in the actual entity class rather than this cast
    // (because dynamic proxies will fool it)
    ManagedType<X> jpaType = em.getMetamodel().managedType((Class<X>) lhs.getClass());

    for (Attribute<? super X, ?> attr : jpaType.getAttributes()) {
      Object lhsVal = get(attr, lhs);
      Object rhsVal = get(attr, rhs);

      if (lhsVal == null && rhsVal == null) return false;
      if (lhsVal == null || rhsVal == null) return true;

      assert (lhsVal != null);
      assert (rhsVal != null);

      switch (attr.getPersistentAttributeType()) {
      case BASIC:
      case ELEMENT_COLLECTION:
        if (!lhsVal.equals(rhsVal)) return true;
        break;

      case EMBEDDED:
      case MANY_TO_ONE:
      case ONE_TO_ONE:
        if (isDifferent(lhsVal, rhsVal, encountered)) return true;
        break;

      case MANY_TO_MANY:
      case ONE_TO_MANY:
        Collection<?> lhsCollection = (Collection<?>) lhs;
        Collection<?> rhsCollection = (Collection<?>) rhs;
        if (lhsCollection.size() != rhsCollection.size()) return true;
        Iterator<?> lhsIt = lhsCollection.iterator();
        Iterator<?> rhsIt = rhsCollection.iterator();
        while (lhsIt.hasNext()) {
          if (isDifferent(lhsIt.next(), rhsIt.next())) return true;
        }
        break;

      default:
        throw new RuntimeException("Unknown JPA attribute type: " + attr.getPersistentAttributeType());
      }
    }

    return false;
  }

  /**
   * Returns the ID of the given object, which must be a JPA entity.
   *
   * @param entity
   *          the JPA entity whose ID value to retrieve
   * @return The ID of the given entity. If the entity ID type is primitive (for
   *         example, {@code int} as opposed to {@code Integer}), the
   *         corresponding boxed value will be returned.
   */
  private <X> Object id(X entity) {
    // XXX probably need to pass in the actual entity class rather than this cast
    // (because dynamic proxies will fool it)
    EntityType<X> type = em.getMetamodel().entity((Class<X>) entity.getClass());
    SingularAttribute<? super X, ?> attr = type.getId(type.getIdType().getJavaType());
    return get(attr, entity);
  }

  /**
   * Sets the ID of the given object, which must be a JPA entity, to its default
   * value. The default value for reference types is {@code null}; the default
   * value for primitive types is the same as the JVM default value for an
   * uninitialized field.
   *
   * @param entity
   * @return
   */
  private <X> void clearId(X entity) {
    // XXX probably need to pass in the actual entity class rather than this cast
    // (because dynamic proxies will fool it)
    EntityType<X> type = em.getMetamodel().entity((Class<X>) entity.getClass());
    SingularAttribute<? super X, ?> attr = type.getId(type.getIdType().getJavaType());
    set(attr, entity, null);
  }

  // TODO make abstract; implement separately for client and server
  private <X, Y> void set(Attribute<X, Y> attr, X entity, Y value) {
    Member member = attr.getJavaMember();
    try {
      if (member instanceof Field) {
        Field f = (Field) member;
        f.setAccessible(true);
        f.set(entity, null); // TODO handle primitive values
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        m.setAccessible(true);
        m.invoke(entity, (Object) null); // TODO handle primitive values
      }
      else {
        throw new RuntimeException("Java member " + member + " isn't a field or a method! Eek!");
      }
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  // TODO make abstract; implement separately for client and server
  private <X, Y> Y get(Attribute<X, Y> attr, X entity) {
    Member member = attr.getJavaMember();
    try {
      if (member instanceof Field) {
        Field f = (Field) member;
        f.setAccessible(true);
        return attr.getJavaType().cast(f.get(entity));
      }
      else if (member instanceof Method) {
        Method m = (Method) member;
        m.setAccessible(true);
        return attr.getJavaType().cast(m.invoke(entity));
      }
      else {
        throw new RuntimeException("Java member " + member + " isn't a field or a method! Eek!");
      }
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
/*
 * Data Sync Plan
 * 1. start with cold refresh of a named query
 * 2. add entity listeners (default entity listener looks right) that will refresh automatically
 * 3. make the API awesome
 */
