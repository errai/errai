package org.jboss.errai.ui.nav.client.local.testpages;

import com.google.common.collect.ImmutableMultimap;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.PageHiding;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.PageShown;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for test pages to record the calls to lifecycle methods.
 *
 * Used in tests to ensure the correct order lifecycle methods.
 *
 * @author Johannes Barop <jb@barop.de>
 */
public class BasePageForLifecycleTracing extends HorizontalPanel {

  public class Record {

    public Class<? extends Annotation> lifecycleAnnotation;

    public Class<? extends Widget> page;

    Record(Class<? extends Annotation> lifecycleAnnotation) {
      this.lifecycleAnnotation = lifecycleAnnotation;
      this.page = BasePageForLifecycleTracing.this.getClass();
    }

    @Override
    public String toString() {
      return "Record{" +
              "lifecycleAnnotation=" + lifecycleAnnotation +
              ", page=" + page +
              '}';
    }

  }

  public static List<Record> lifecycleTracer =  new LinkedList<Record>();

  public Class<? extends Widget> redirectPage;

  @Inject
  public Navigation navigation;

  protected void doRedirect() {
    if (redirectPage != null) {
      navigation.goTo(redirectPage, ImmutableMultimap.<String, String>of());
    }
  }

  @PageShowing
  protected void beforeShow() {
    lifecycleTracer.add(new Record(PageShowing.class));
     doRedirect();
  }

  @PageShown
  private void afterShown() {
    lifecycleTracer.add(new Record(PageShown.class));
  }

  @PageHiding
  protected void beforeHide() {
    lifecycleTracer.add(new Record(PageHiding.class));
  }

  @PageHidden
  private void afterHide() {
    lifecycleTracer.add(new Record(PageHidden.class));
  }

}
