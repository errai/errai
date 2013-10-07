package org.jboss.errai.cdi.event.client.shared;

import org.jboss.errai.common.client.api.annotations.LocalEvent;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
@LocalEvent
public class PortableLocalEventA {
  public String subject;
}
