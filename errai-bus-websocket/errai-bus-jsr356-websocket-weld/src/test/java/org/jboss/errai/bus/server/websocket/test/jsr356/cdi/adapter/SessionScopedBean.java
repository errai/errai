package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.adapter;

import javax.enterprise.context.SessionScoped;

import java.io.Serializable;

/**
 * @author Michel Werren
 */
@SessionScoped
public class SessionScopedBean implements Serializable {
  private static final long serialVersionUID = -7302123161109855379L;

  private Short id;

  public Short getId() {
    return id;
  }

  public void setId(Short id) {
    this.id = id;
  }

}
