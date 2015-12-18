/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.jpa.sync.client.shared;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.jboss.errai.common.client.api.Assert;

public class EntityComparator {

  private final Metamodel metamodel;
  private final JpaAttributeAccessor attributeAccessor;

  public EntityComparator(Metamodel metamodel, JpaAttributeAccessor attributeAccessor) {
    this.metamodel = Assert.notNull(metamodel);
    this.attributeAccessor = Assert.notNull(attributeAccessor);
  }

  /**
   * Compares two JPA Managed Type instances to see if they are the same.
   *
   * @param lhs
   * @param rhs
   * @return
   */
  public <X> boolean isDifferent(X lhs, X rhs) {
    return isDifferent(lhs, rhs, metamodel, new IdentityHashMap<Object, Object>());
  }

  /**
   * Private recursive subroutine of {@link #isDifferent(Object, Object)}.
   *
   * @param lhs
   * @param rhs
   * @param encountered
   * @return
   */
  private <X> boolean isDifferent(X lhs, X rhs, Metamodel metamodel, IdentityHashMap<Object, Object> encountered) {
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
    @SuppressWarnings("unchecked")
    ManagedType<X> jpaType = metamodel.managedType((Class<X>) lhs.getClass());

    for (Attribute<? super X, ?> attr : jpaType.getAttributes()) {
      Object lhsVal = attributeAccessor.get(attr, lhs);
      Object rhsVal = attributeAccessor.get(attr, rhs);

      if (lhsVal == null && rhsVal == null) continue;
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
        if (isDifferent(lhsVal, rhsVal, metamodel, encountered)) return true;
        break;

      case MANY_TO_MANY:
      case ONE_TO_MANY:
        Collection<?> lhsCollection = (Collection<?>) lhsVal;
        Collection<?> rhsCollection = (Collection<?>) rhsVal;
        if (lhsCollection.size() != rhsCollection.size()) return true;
        Iterator<?> lhsIt = lhsCollection.iterator();
        Iterator<?> rhsIt = rhsCollection.iterator();
        while (lhsIt.hasNext()) {
          // FIXME this will not work for unordered collections (eg. bags, sets). Needs tests!
          if (isDifferent(lhsIt.next(), rhsIt.next(), metamodel, encountered)) return true;
        }
        break;

      default:
        throw new RuntimeException("Unknown JPA attribute type: " + attr.getPersistentAttributeType());
      }
    }

    return false;
  }
}
