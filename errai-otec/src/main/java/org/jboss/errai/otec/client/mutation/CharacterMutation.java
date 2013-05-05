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

package org.jboss.errai.otec.client.mutation;

import org.jboss.errai.otec.client.StringState;


/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class CharacterMutation implements Mutation<StringState, Character> {
  private final MutationType type;
  private final int position;
  private final char data;

  private CharacterMutation(final MutationType type, final int position, final char data) {
    this.type = type;
    this.position = position;
    this.data = data;
  }

  public static CharacterMutation noop(final int position) {
    return of(MutationType.Noop, position, (char) 0);
  }

  public static CharacterMutation of(final MutationType type, final int position, final char data) {
    return new CharacterMutation(type, position, data);
  }

  @Override
  public MutationType getType() {
    return type;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public Character getData() {
    return data;
  }

  @Override
  public int length() {
    return 1;
  }

  @Override
  public void apply(final StringState state) {
    switch (type) {
      case Insert:
        state.insert(position, data);
        break;
      case Delete:
        state.delete(position);
        break;
    }
  }

  @Override
  public Mutation<StringState, Character> combineWith(Mutation<StringState, Character> combine) {
    return null;
  }

  @Override
  public Mutation<StringState, Character> newBasedOn(final int index) {
    return of(type, index, data);
  }

  @Override
  public Mutation<StringState, Character> newBasedOn(int index, int truncate) {
    return newBasedOn(index);
  }

  @Override
  public String toString() {
    if (getData() == null || getData() == 0) {
      return type.getShortName() + "[" + getPosition() + "]";
    }
    else {
      return type.getShortName() + "[" + getPosition() + ",\"" + getData() + "\"]";
    }
  }
}

