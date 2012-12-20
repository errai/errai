/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.uibinder.test.client.res;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

/**
 * @author Mike Brock
 */
@Singleton
public class HelloWorld {
  @Inject UiBinder<DivElement, HelloWorld> binder;
  @Inject TestSafeTemplates safeTemplates;

  @UiField SpanElement nameSpan;

  @PostConstruct
  public void postConstr() {
    binder.createAndBindUi(this);
  }

  public UiBinder<DivElement, HelloWorld> getBinder() {
    return binder;
  }

  public SpanElement getNameSpan() {
    return nameSpan;
  }

  public TestSafeTemplates getSafeTemplates() {
    return safeTemplates;
  }

  public interface TestSafeTemplates extends SafeHtmlTemplates {
    @Template("<a target=\"_blank\" href=\"{0}\">{1}</a>")
    SafeHtml link(SafeUri url, String linkText);
  }
}
