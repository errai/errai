package org.jboss.errai.jpa.client.local;

import javax.persistence.Parameter;

public class ErraiParameter<T> implements Parameter<T> {

  private final String name;
  private final Integer position;
  private final Class<T> type;

  ErraiParameter(String name, Integer position, Class<T> type) {
    this.name = name;
    this.position = position;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Integer getPosition() {
    return position;
  }

  @Override
  public Class<T> getParameterType() {
    return type;
  }
}
