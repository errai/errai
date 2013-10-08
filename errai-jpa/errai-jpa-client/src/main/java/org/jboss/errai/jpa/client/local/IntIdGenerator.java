package org.jboss.errai.jpa.client.local;



public class IntIdGenerator<X> implements ErraiIdGenerator<Integer> {

  private final ErraiSingularAttribute<X, Integer> attr;

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

  public IntIdGenerator(ErraiSingularAttribute<X, Integer> attr) {
    this.attr = attr;
  }

  /**
   * We assume there is always the possibility to find another unused Integer value.
   *
   * @return true
   */
  @Override
  public boolean hasNext(ErraiEntityManager entityManager) {
    return true;
  }


  @Override
  public Integer next(ErraiEntityManager entityManager) {
    while (entityManager.isKeyInUse(new Key<X, Integer>((ErraiIdentifiableType<X>) attr.getDeclaringType(), nextCandidateId))) {
      nextCandidateId += (int) (Math.random() * probeJumpSize);

      // control rollover in case we run out of values
      if (nextCandidateId >= Integer.MAX_VALUE - probeJumpSize) {
        nextCandidateId = 1;
      }
    }

    return nextCandidateId++;
  }

}
