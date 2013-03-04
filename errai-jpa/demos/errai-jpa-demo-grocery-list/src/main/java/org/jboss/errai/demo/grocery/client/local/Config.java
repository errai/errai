package org.jboss.errai.demo.grocery.client.local;

import org.jboss.errai.bus.client.framework.Configuration;

/**
 * @author edewit@redhat.com
 */
public class Config implements Configuration {
  @Override
  public String getRemoteLocation() {
    //return "https://grocery-edewit.rhcloud.com/errai-jpa-demo-grocery-list";
    return "http://127.0.0.1:8888/";
  }
}
