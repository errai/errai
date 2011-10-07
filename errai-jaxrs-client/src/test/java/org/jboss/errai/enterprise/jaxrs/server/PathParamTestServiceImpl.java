package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.PathParamTestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PathParamTestServiceImpl implements PathParamTestService {

  @Override
  public long getWithPathParam(long id) {
    return id;
  }

  @Override
  public String getWithMultiplePathParams(long id1, long id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public String getWithReusedPathParam(long id1, long id2) {
    return "" + id1 + "/" + id2 + "/" + id1;
  }
  
  @Override
  public long postWithPathParam(long id) {
    return id;
  }

  @Override
  public long putWithPathParam(long id) {
    return id;
  }

  @Override
  public long deleteWithPathParam(long id) {
    return id;
  }
}
