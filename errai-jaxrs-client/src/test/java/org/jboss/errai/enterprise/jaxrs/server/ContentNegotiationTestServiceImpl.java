package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.ContentNegotiationTestService;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class ContentNegotiationTestServiceImpl implements ContentNegotiationTestService {

  @Override
  public String getText() {
    return "text";
  }

  @Override
  public String getXml() {
    return "xml";
  }

  @Override
  public String postText(String text) {
    return "post:"+text;
  }

  @Override
  public String postXml(String xml) {
    return "post:"+xml;
  }

  @Override
  public String putText(String text) {
    return "put:"+text;
  }

  @Override
  public String putXml(String xml) {
    return "put:"+xml;
  }

  @Override
  public String deleteText(String text) {
    return "delete:text";
  }

  @Override
  public String deleteXml(String xml) {
    return "delete:xml";
  }
}