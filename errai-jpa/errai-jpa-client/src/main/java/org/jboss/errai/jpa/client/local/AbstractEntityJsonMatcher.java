package org.jboss.errai.jpa.client.local;

/**
 * I'm not a huge fan of this "setQuery() matches(), matches(), ..." approach. A
 * refactoring between ErraiTypedQuery and StorageBackend might sort things out.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class AbstractEntityJsonMatcher implements EntityJsonMatcher {

  protected ErraiTypedQuery<?> query;

  @Override
  public void setQuery(ErraiTypedQuery<?> query) {
    this.query = query;
  }

  @Override
  public ErraiTypedQuery<?> getQuery() {
    return query;
  }
}
