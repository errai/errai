package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.HeaderParamTestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class HeaderParamTestServiceImpl implements HeaderParamTestService {

  @Override
  public String getWithHeaderParam(String id) {
    return id;
  }

  @Override
  public String getWithMultipleHeaderParams(String id1, String id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public String postWithHeaderParam(String id) {
    return id;
  }

  @Override
  public String putWithHeaderParam(String id) {
    return id;
  }

  @Override
  public String deleteWithHeaderParam(String id) {
    return id;
  }
}
