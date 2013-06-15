package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.util.state.State;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simple test page for tracing lifecycle methods.
 *
 * @author Johannes Barop <jb@barop.de>
 */
@ApplicationScoped @Page
public class PageAWithRedirect extends BasePageForLifecycleTracing {

}
