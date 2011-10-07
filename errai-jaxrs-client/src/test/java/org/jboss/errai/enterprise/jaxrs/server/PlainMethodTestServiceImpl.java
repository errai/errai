package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.PlainMethodTestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PlainMethodTestServiceImpl implements PlainMethodTestService {

  @Override
  public String get() {
    return "get";
  }

  @Override
  public String post() {
    return "post";
  }

  @Override
  public String put() {
    return "put";
  }

  @Override
  public String delete() {
    return "delete";
  }
  
  @Override
  public String head() {
    return "head";
  }
}
