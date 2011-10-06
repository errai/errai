package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.TestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class TestServiceImpl implements TestService {

  @Override
  public String noParamGet() {
    return "get";
  }

  @Override
  public String noParamPost() {
    return "post";
  }

  @Override
  public String noParamPut() {
    return "put";
  }

  @Override
  public String noParamDelete() {
    return "delete";
  }

}
