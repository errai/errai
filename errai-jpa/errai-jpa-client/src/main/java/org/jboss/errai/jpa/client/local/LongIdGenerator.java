package org.jboss.errai.jpa.client.local;

import java.util.Iterator;

public class LongIdGenerator implements Iterator<Long> {

  private final ErraiEntityManager entityManager;
  private final ErraiSingularAttribute<?, Long> attr;

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

  public LongIdGenerator(ErraiEntityManager entityManager, ErraiSingularAttribute<?, Long> attr) {
    this.entityManager = entityManager;
    this.attr = attr;
  }

  /**
   * We assume there is always the possibility to find another unused Long value.
   *
   * @return true
   */
  @Override
  public boolean hasNext() {
    return true;
  }


  @Override
  public Long next() {
    while (entityManager.find(attr.getDeclaringType().getJavaType(), nextCandidateId) != null) {
      nextCandidateId += (long) (Math.random() * probeJumpSize);

      // control rollover in case we run out of values
      if (nextCandidateId >= Long.MAX_VALUE - probeJumpSize) {
        nextCandidateId = 0;
      }
    }

    return nextCandidateId++;
  }

  /**
   * Not a supported operation.
   *
   * @throws {@link UnsupportedOperationException} on every invocation.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

}
