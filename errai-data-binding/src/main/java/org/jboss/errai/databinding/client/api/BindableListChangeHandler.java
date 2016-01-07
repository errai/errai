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

package org.jboss.errai.databinding.client.api;

import java.util.Collection;
import java.util.List;

import org.jboss.errai.databinding.client.BindableListWrapper;

/**
 * A change handler for monitoring mutations of bindable lists (see {@link BindableListWrapper}).
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface BindableListChangeHandler<M> {

  /**
   * Called when a single item has been appended to the list.
   * 
   * @param source
   *          a list representing the state before the item was appended (equal to the old value of the
   *          list). Never null.
   * @param item
   *          the added item. May be null.
   */
  public void onItemAdded(List<M> source, M item);

  /**
   * Called when a single item has been added to the list at the provided index.
   * 
   * @param source
   *          a list representing the state before the item was added (equal to the old value of the
   *          list). Never null.
   * @param index
   *          the index at which the item has been added.
   * @param item
   *          the added item. May be null.
   */
  public void onItemAddedAt(List<M> source, int index, M item);

  /**
   * Called when multiple items have been appended to the list.
   * 
   * @param source
   *          a list representing the state before the items were appended (equal to the old value of
   *          the list). Never null.
   * @param items
   *          the added items. May contain null elements.
   */
  public void onItemsAdded(List<M> source, Collection<? extends M> items);

  /**
   * Called when multiple items have been added to the list starting at the provided index.
   * 
   * @param source
   *          a list representing the state before the items were added (equal to the old value of
   *          the list). Never null.
   * @param index
   *          the index at which the items have been added.
   * @param item
   *          the added items. May contain null elements.
   */
  public void onItemsAddedAt(List<M> source, int index, Collection<? extends M> items);

  /**
   * Called when all items have been removed from the list.
   * 
   * @param source
   *          a list representing the state before all items were removed (equal to the old value of
   *          the list). Never null.
   */
  public void onItemsCleared(List<M> source);

  /**
   * Called when a single item has been removed from the list at the provided index.
   * 
   * @param source
   *          a list representing the state before the item was removed (equal to the old value of
   *          the list). Never null.
   * @param index
   *          the index at which the item has been removed.
   */
  public void onItemRemovedAt(List<M> source, int index);

  /**
   * Called when multiple items have been removed from the list.
   * 
   * @param source
   *          a list representing the state before the items were removed (equal to the old value of
   *          the list). Never null.
   * @param index
   *          the indexes at which items have been removed.
   */
  public void onItemsRemovedAt(List<M> source, List<Integer> indexes);

  /**
   * Called when a single item has been changed.
   * 
   * @param source
   *          a list representing the state before the item was changed (equal to the old value of
   *          the list). Never null.
   * @param index
   *          the index of the item that has changed.
   * @param item
   *          the new value of the item at the provided index.
   */
  public void onItemChanged(List<M> source, int index, M item);
  
}
