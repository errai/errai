package org.jboss.errai.ui.test.nested.client.res;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;

@Templated("A.html")
public class NestedClassComponent extends Composite {
  @Inject @DataField Content b;
  
  @Templated("B.html")  
  public static class Content extends Composite {  

  }  

}
