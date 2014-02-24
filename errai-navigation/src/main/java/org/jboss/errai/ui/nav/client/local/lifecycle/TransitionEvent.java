package org.jboss.errai.ui.nav.client.local.lifecycle;

import org.jboss.errai.ioc.client.lifecycle.api.LifecycleEvent;

import com.google.gwt.user.client.ui.IsWidget;

public interface TransitionEvent<W extends IsWidget> extends LifecycleEvent<W> {

}
