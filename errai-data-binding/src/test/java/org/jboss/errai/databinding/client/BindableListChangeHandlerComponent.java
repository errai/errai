/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.common.client.function.Optional;
import org.jboss.errai.databinding.client.api.handler.list.BindableListChangeHandler;

import com.google.gwt.user.client.TakesValue;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class BindableListChangeHandlerComponent implements TakesValue<List<TestModel>>, BindableListChangeHandler<TestModel> {

  private List<TestModel> boundCopy = new ArrayList<>();

  @Override
  public void onItemAdded(List<TestModel> source, TestModel item) {
    boundCopy.add(unwrap(item));
  }

  @Override
  public void onItemAddedAt(List<TestModel> source, int index, TestModel item) {
    boundCopy.add(index, unwrap(item));
  }

  @Override
  public void onItemsAdded(List<TestModel> source, Collection<? extends TestModel> items) {
    for (final TestModel model : items) {
      boundCopy.add(unwrap(model));
    }
  }

  @Override
  public void onItemsAddedAt(List<TestModel> source, int index, Collection<? extends TestModel> items) {
    int i = index;
    for (final TestModel model : items) {
      boundCopy.add(i++, unwrap(model));
    }
  }

  @Override
  public void onItemRemovedAt(List<TestModel> source, int index) {
    boundCopy.remove(index);
  }

  @Override
  public void onItemsRemovedAt(List<TestModel> source, List<Integer> indexes) {
    Collections.sort(indexes, (n, m) -> m - n);
    for (final int i : indexes) {
      boundCopy.remove(i);
    }
  }

  @Override
  public void onItemsCleared(List<TestModel> source) {
    boundCopy.clear();
  }

  @Override
  public void onItemChanged(List<TestModel> source, int index, TestModel item) {
    boundCopy.set(index, unwrap(item));
  }

  @SuppressWarnings("unchecked")
  private static TestModel unwrap(TestModel model) {
    return ((BindableProxy<TestModel>) model).deepUnwrap();
  }

  @Override
  public List<TestModel> getValue() {
    return boundCopy;
  }

  @Override
  public void setValue(final List<TestModel> value) {
    onItemsCleared(null);
    Optional.ofNullable(value).ifPresent(v -> onItemsAdded(null, v));
  }

}
