package org.jboss.errai.jpa.client.local;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.jboss.errai.jpa.test.entity.Album;

/**
 * Temporary proof-of-concept implementation. Real impls will be code generated.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class TestingTypedQueryFactory implements TypedQueryFactory {
  ErraiEntityManager entityManager;

  public TestingTypedQueryFactory(ErraiEntityManager entityManager) {
    this.entityManager = entityManager;
  }

  private Class<Album> actualTargetType = Album.class;
  @Override
  public TypedQuery createIfCompatible(Class resultType) {
    if (resultType != actualTargetType) {
      throw new IllegalArgumentException("Expected return type " + resultType + " is not assignable from actual return type " + actualTargetType);
    }
    ErraiTypedQuery<Album> query = new ErraiTypedQuery<Album>(
            new ErraiParameter<String>("name", 0, String.class)) {

      @Override
      public List<Album> getResultList() {
        List<Album> results = new ArrayList<Album>();

        // probably have to separate the operations:
        //  1. retrieving an entity from the backend
        //  2. hooking up references to related entities
        //  3. adding retrieved stuff to the persistence context
        // OR
        // generate query matchers that work on the JSON representation, and pass them to the backend

        List<Album> everything = entityManager.findAll(entityManager.getMetamodel().entity(Album.class));

        for (Album r : everything) {

          // this is an example of the code that would be generated for
          // "WHERE a.name = :name"
          if (r.getName() != null && r.getName().equals(getParameterValue("name"))) {
            results.add(r);
          }
        }

        return results;
      }
    };
    return query;
  }
}