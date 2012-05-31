package org.jboss.errai.jpa.client.local;

import java.util.Iterator;

public class IntIdGenerator implements Iterator<Integer> {

  private final ErraiEntityManager entityManager;
  private final ErraiSingularAttribute<?, Integer> attr;

  /**
   * The next ID we will attempt to return (after checking for collisions in the datastore).
   */
  private int nextCandidateId = 1;

  /**
   * When the next candidate ID value collides with a value in the database, we
   * skip up to this many values in the sequence before trying again. The reason
   * for the randomness is to avoid probing the same values again after a page
   * reload.
   */
  private final double probeJumpSize = 1000;

  public IntIdGenerator(ErraiEntityManager entityManager, ErraiSingularAttribute<?, Integer> attr) {
    this.entityManager = entityManager;
    this.attr = attr;
  }

  /**
   * We assume there is always the possibility to find another unused Integer value.
   *
   * @return true
   */
  @Override
  public boolean hasNext() {
    return true;
  }


  @Override
  public Integer next() {
    while (entityManager.find(attr.getDeclaringType().getJavaType(), nextCandidateId) != null) {
      nextCandidateId += (int) (Math.random() * probeJumpSize);

      // control rollover in case we run out of values
      if (nextCandidateId >= Integer.MAX_VALUE - probeJumpSize) {
        nextCandidateId = 1;
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
