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
