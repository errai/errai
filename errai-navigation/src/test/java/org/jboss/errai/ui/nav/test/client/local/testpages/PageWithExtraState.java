package org.jboss.errai.ui.nav.test.client.local.testpages;

import org.jboss.errai.ui.nav.client.local.Page;

import com.google.gwt.user.client.ui.SimplePanel;

@Page(path="{stringThing}/{intThing}" /*, state=MyState.class*/)
public class PageWithExtraState extends SimplePanel {

  // The commented code below is an idea for how the "extra state" feature should work:

//  static class MyState {
//    String stringThing;
//    int intThing;
//  }
//
//  @PageLifecycle
//  private void onPageShowing(final MyState myState) {
//    rpcCall(myState, new SuccessCallback() {
//      public void onSuccess() {
//        productBinder.setModel(product, InitialState.FROM_MODEL);
//      }
//    });
//  }
}
