package org.jboss.errai.aerogear.api.pipeline;

/**
 * @author edewit@redhat.com
 */
public enum PipeType {

  REST("Rest");
  private final String name;

  PipeType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
