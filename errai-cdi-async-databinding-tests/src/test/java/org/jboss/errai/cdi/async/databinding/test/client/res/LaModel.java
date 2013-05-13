package org.jboss.errai.cdi.async.databinding.test.client.res;

import org.jboss.errai.databinding.client.api.Bindable;

/**
 * @author Mike Brock
 */
@Bindable
public class LaModel {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
