/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  private Class<? extends Widget> redirectPage;

  @Inject
  protected Navigation navigation;

  public void setRedirectPage(Class<? extends Widget> page) {
    redirectPage = page;
  }

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
