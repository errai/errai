package org.jboss.errai.jpa.client.local;

import java.math.BigInteger;
import java.util.Iterator;

public class BigIntegerIdGenerator implements Iterator<BigInteger> {

  private final ErraiEntityManager entityManager;
  private final ErraiSingularAttribute<?, BigInteger> attr;

  /**
   * The next ID we will attempt to return (after checking for collisions in the datastore).
   */
  private BigInteger nextCandidateId = BigInteger.ONE;

  /**
   * When the next candidate ID value collides with a value in the database, we
   * skip up to this many values in the sequence before trying again. The reason
   * for the randomness is to avoid probing the same values again after a page
   * reload.
   */
  private final double probeJumpSize = 1000;

  public BigIntegerIdGenerator(ErraiEntityManager entityManager, ErraiSingularAttribute<?, BigInteger> attr) {
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
  public BigInteger next() {
    BigInteger nextAvailableId = nextCandidateId;
    while (entityManager.find(attr.getDeclaringType().getJavaType(), nextAvailableId,
            LongIdGenerator.NO_SIDE_EFFECTS_OPTION) != null) {
      nextAvailableId = nextAvailableId.add(new BigInteger(String.valueOf(Math.random() * probeJumpSize)));
    }
    nextCandidateId = nextAvailableId.add(BigInteger.ONE);
    return nextAvailableId;
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
