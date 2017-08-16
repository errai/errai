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

package org.jboss.errai.databinding.client;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
@Dependent
public class HasBoundElemental2IsElement {

  @Inject
  @AutoBound
  private DataBinder<TestModel> binder;

  @Inject
  @Bound(property = "value")
  private SimpleHTMLInputElementPresenter textPresenter;

  @Inject
  @Bound(property = "active")
  private TakesValueElemental2CheckInputPresenter checkPresenter;

  public SimpleHTMLInputElementPresenter getTextPresenter() {
    return textPresenter;
  }

  public TakesValueElemental2CheckInputPresenter getCheckPresenter() {
    return checkPresenter;
  }

  public DataBinder<TestModel> getBinder() {
    return binder;
  }

}
