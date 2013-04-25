package org.jboss.errai.cdi.demo.mvp.client.local.presenter;

import com.google.gwt.user.client.ui.HasWidgets;

public abstract interface Presenter {
  public abstract void go(final HasWidgets container);
}
