package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.ContentNegotiationTestService;

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
    return "text:"+text;
  }

  @Override
  public String postXml(String xml) {
    return "xml:"+xml;
  }

  @Override
  public String putText(String text) {
    return "text:"+text;
  }

  @Override
  public String putXml(String xml) {
    return "xml:"+xml;
  }

  @Override
  public String deleteText(String text) {
    return "text:"+text;
  }

  @Override
  public String deleteXml(String xml) {
    return "xml:"+xml;
  }
}