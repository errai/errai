package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.adapter;

import javax.enterprise.context.ConversationScoped;

import java.io.Serializable;

/**
 * @author Michel Werren
 */
@ConversationScoped
public class ConversationScopeBean implements Serializable {
  private static final long serialVersionUID = -5079575673737823581L;

  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
