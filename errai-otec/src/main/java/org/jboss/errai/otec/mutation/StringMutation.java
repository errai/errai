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

package org.jboss.errai.otec.mutation;

import org.jboss.errai.otec.StringState;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class StringMutation implements Mutation<StringState, IndexPosition, CharacterData> {
  private final MutationType type;
  private final IndexPosition position;
  private final CharacterData data;

  public StringMutation(final MutationType type, final IndexPosition position, final CharacterData data) {
    this.type = type;
    this.position = position;
    this.data = data;
  }

  @Override
  public MutationType getType() {
    return type;
  }

  @Override
  public IndexPosition getPosition() {
    return position;
  }

  @Override
  public CharacterData getData() {
    return data;
  }

  @Override
  public void apply(final StringState state) {
    switch (type) {
      case Insert:
        state.insert(position.getPosition(), data.get());
        break;
      case Delete:
        state.delete(position.getPosition());
        break;
      case Retain:
        break;
    }
  }

  @Override
  public String toString() {
    return type + "[" + getPosition() + ", \"" + getData() + "\"]";
  }
}

