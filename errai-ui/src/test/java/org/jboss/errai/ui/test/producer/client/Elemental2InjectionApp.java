/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.test.producer.client;

import com.google.gwt.dom.client.DivElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@EntryPoint
public class Elemental2InjectionApp {

  @Inject
  HTMLDivElement elemental2HtmlDivElement; //works because of ElementProviderExtension

  @Inject
  Div erraiDomWrapperDiv; //works because of ElementProviderExtension

  @Inject
  DivElement gwtElementDiv; //works because of ElementProviderExtension

  @Inject
  @Named("span")
  HTMLElement elemental2HtmlElementSpan; //works because of Elemental2HTMLElementProvider

  @Inject
  @Named("div")
  HTMLElement elemental2HtmlElementDiv; //works because of Elemental2HTMLElementProvider

  @Inject
  @Named("div")
  org.jboss.errai.common.client.dom.HTMLElement erraiDomWrapperHTMLElementDiv; //works because of HTMLElementProvider

  @PostConstruct
  public void setup() {
  }

}
