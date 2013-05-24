/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.databinding.client;

import java.util.Collection;
import java.util.List;

/**
 * A change handler for monitoring changes of bindable lists (see {@link BindableListWrapper}).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableListChangeHandler<M> {
  public void onItemAdded(List<M> source, M item);

  public void onItemAddedAt(List<M> source, int index, M item);

  public void onItemsAdded(List<M> source, Collection<? extends M> items);

  public void onItemsAddedAt(List<M> source, int index, Collection<? extends M> items);

  public void onItemsCleared(List<M> source);

  public void onItemRemovedAt(List<M> source, int index);

  public void onItemsRemovedAt(List<M> source, List<Integer> indexes);
  
  public void onItemChanged(List<M> source, int index, M item);
}
