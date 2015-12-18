/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.grocery.client.local;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.google.gwt.user.client.ui.ValueListBox;

/**
 * @author edewit@redhat.com
 */
@ApplicationScoped
public class ValueListBoxProducer {

  @Produces
  public ValueListBox<Integer> createValueListBox() {
    Collection<Integer> values = new ArrayList<Integer>();
    values.add(Integer.valueOf(1));
    values.add(Integer.valueOf(2));
    values.add(Integer.valueOf(5));
    values.add(Integer.valueOf(10));
    values.add(Integer.valueOf(25));

    final ValueListBox<Integer> radiusPicker = new ValueListBox<Integer>();
    radiusPicker.setValue(Integer.valueOf(25));
    radiusPicker.setAcceptableValues(values);
    return radiusPicker;
  }
}
