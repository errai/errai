package org.jboss.errai.ui.client.local.spi;

import com.google.gwt.core.client.GWT;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 * Helper to get an obfuscated less style.
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class LessStyle {

  private LessStyleMapping lessStyleMapping;

  @PostConstruct
  public void init() {
    lessStyleMapping = GWT.create(LessStyleMapping.class);
  }

  public String get(String style) {
    return lessStyleMapping.get(style);
  }
}
