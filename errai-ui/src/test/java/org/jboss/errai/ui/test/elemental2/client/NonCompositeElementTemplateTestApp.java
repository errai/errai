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

package org.jboss.errai.ui.test.elemental2.client;

import com.google.gwt.user.client.ui.RootPanel;
import jsinterop.base.Js;
import org.jboss.errai.ui.test.elemental2.client.res.ElementFormComponent;
import org.jboss.errai.ui.test.elemental2.client.res.NonCompositeElementFormComponent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@Dependent
public class NonCompositeElementTemplateTestApp implements ElementTemplateTestApp {

  @Inject
  private RootPanel root;

  @Inject
  private NonCompositeElementFormComponent form;

  @PostConstruct
  public void setup() {
    root.getElement().appendChild(Js.cast(form.getForm()));
  }

  @Override
  public ElementFormComponent getForm() {
    return form;
  }
}
