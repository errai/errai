package org.jboss.errai.validation.client;

import javax.validation.constraints.NotNull;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.junit.Ignore;

@Bindable
@Portable
@Ignore
public class BlacklistedWithConstraint {

  @NotNull
  private Object o = new Object();
  
}
