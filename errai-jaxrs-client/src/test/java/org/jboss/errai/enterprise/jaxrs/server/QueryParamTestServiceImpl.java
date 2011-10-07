package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.QueryParamTestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class QueryParamTestServiceImpl implements QueryParamTestService {

  @Override
  public long getWithQueryParam(long id) {
    return id;
  }

  @Override
  public String getWithMultipleQueryParams(long id1, long id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public long postWithQueryParam(long id) {
    return id;
  }

  @Override
  public long putWithQueryParam(long id) {
    return id;
  }

  @Override
  public long deleteWithQueryParam(long id) {
    return id;
  }
}
