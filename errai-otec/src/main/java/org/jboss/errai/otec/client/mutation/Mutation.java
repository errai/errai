/**
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

package org.jboss.errai.otec.client.mutation;

import org.jboss.errai.otec.client.State;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public interface Mutation<T extends State, D> {
  public MutationType getType();
  public int getPosition();
  public D getData();
  public int length();
  public void apply(T state);
  public Mutation<T, D> newBasedOn(int index);
  public Mutation<T, D> newBasedOn(int index, int truncate);
  public Mutation<T, D> combineWith(Mutation<T, D> combine);

}
