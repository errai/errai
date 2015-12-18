package org.jboss.errai.validation.client.blacklisted;

import javax.validation.constraints.NotNull;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.junit.Ignore;

@Bindable
@Portable
@Ignore
public class ClassWithConstraintInBlacklistedPackage {

  @NotNull
  private Object o = new Object();

}
