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

package org.jboss.errai.jpa.client.local;



public class LongIdGenerator<X> implements ErraiIdGenerator<Long> {

  private final ErraiSingularAttribute<X, Long> attr;

  /**
   * The next ID we will attempt to return (after checking for collisions in the datastore).
   */
  private long nextCandidateId = 1L;

  /**
   * When the next candidate ID value collides with a value in the database, we
   * skip up to this many values in the sequence before trying again. The reason
   * for the randomness is to avoid probing the same values again after a page
   * reload.
   */
  private final double probeJumpSize = 1000;

  public LongIdGenerator(ErraiSingularAttribute<X, Long> attr) {
    this.attr = attr;
  }

  /**
   * We assume there is always the possibility to find another unused Long value.
   *
   * @return true
   */
  @Override
  public boolean hasNext(ErraiEntityManager entityManager) {
    return true;
  }


  @Override
  public Long next(ErraiEntityManager entityManager) {
    while (entityManager.isKeyInUse(new Key<X, Long>((ErraiIdentifiableType<X>) attr.getDeclaringType(), nextCandidateId))) {
      nextCandidateId += (long) (Math.random() * probeJumpSize);

      // control rollover in case we run out of values
      if (nextCandidateId >= Long.MAX_VALUE - probeJumpSize) {
        nextCandidateId = 0;
      }
    }

    return nextCandidateId++;
  }
}
