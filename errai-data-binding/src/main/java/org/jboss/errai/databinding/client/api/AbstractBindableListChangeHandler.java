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

package org.jboss.errai.databinding.client.api;

import java.util.Collection;
import java.util.List;

/**
 * A convenience class for implementing {@link BindableListChangeHandler BindableListChangeHandlers} that only wish to
 * implement a subset of the methods from the interface. All implementations in
 * {@link AbstractBindableListChangeHandler} are noops.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class AbstractBindableListChangeHandler<M> implements BindableListChangeHandler<M> {

  @Override
  public void onItemAdded(List<M> source, M item) {
  }

  @Override
  public void onItemAddedAt(List<M> source, int index, M item) {
  }

  @Override
  public void onItemsAdded(List<M> source, Collection<? extends M> items) {
  }

  @Override
  public void onItemsAddedAt(List<M> source, int index, Collection<? extends M> items) {
  }

  @Override
  public void onItemsCleared(List<M> source) {
  }

  @Override
  public void onItemRemovedAt(List<M> source, int index) {
  }

  @Override
  public void onItemsRemovedAt(List<M> source, List<Integer> indexes) {
  }

  @Override
  public void onItemChanged(List<M> source, int index, M item) {
  }

}
