package org.jboss.errai.jpa.sync.client.shared;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.EntityType;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

@Portable
public class SyncableDataSet<E> {

  private final String queryName;
  private final Map<String, Object> params;

  /** Stored as a string so to keep SyncableDataSet marshallable. */
  private final String resultTypeFqcn;


  // FIXME need to allow app to specify TemporalType for date params
  public SyncableDataSet(String queryName, Class<E> resultType, Map<String, Object> params) {
    this(queryName, resultType.getName(), params);
  }

  // Errai Marshalling constructor
  private SyncableDataSet(
          @MapsTo("queryName") String queryName,
          @MapsTo("resultTypeFqcn") String resultTypeFqcn,
          @MapsTo("params") Map<String, Object> params) {
    this.queryName = Assert.notNull(queryName);
    this.resultTypeFqcn = resultTypeFqcn;
    this.params = Assert.notNull(params);
  }

  public TypedQuery<E> createQuery(EntityManager em) {
    TypedQuery<E> query = em.createNamedQuery(queryName, getResultType(em));
    for (Map.Entry<String, Object> param : params.entrySet()) {
      // FIXME should use TemporalType arg here when necessary
      query.setParameter(param.getKey(), param.getValue());
    }
    return query;
  }

  @SuppressWarnings("unchecked")
  private Class<E> getResultType(EntityManager em) {
    for (EntityType<?> et : em.getMetamodel().getEntities()) {
      if (et.getJavaType().getName().equals(resultTypeFqcn)) {
        return ((EntityType<E>) et).getJavaType();
      }
    }
    throw new IllegalStateException("Result type " + resultTypeFqcn + " is not known to the EntityManager.");
  }
}
