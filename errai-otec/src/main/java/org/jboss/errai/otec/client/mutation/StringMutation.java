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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.otec.client.StringState;


/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@Portable
public class StringMutation implements Mutation<StringState, String> {
  private final MutationType type;
  private final int position;
  private final String data;

  private StringMutation(final MutationType type, final int position, final String data) {
    this.type = type;
    this.position = position;
    this.data = data;
  }

  public static StringMutation of(@MapsTo("type") final MutationType type,
                                  @MapsTo("position") final int position,
                                  @MapsTo("data") final String data) {
    return new StringMutation(type, position, data);
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
  public String getData() {
    return data;
  }

  @Override
  public int length() {
    return data == null ? 1 : data.length();
  }

  @Override
  public void apply(final StringState state) {
    switch (type) {
      case Insert:
        state.insert(position, data);
        break;
      case Delete:
        state.delete(position, length());
        break;
    }
  }

  @Override
  public Mutation<StringState, String> newBasedOn(int index) {
    return of(type, index, data);
  }

  @Override
  public Mutation<StringState, String> newBasedOn(int index, int truncate) {
    final String newData;
    if (data.length() > truncate && truncate >= 0) {
      newData = data.substring(0, truncate);
    }
    else {
      newData = data;
    }

    return of(type, index, newData);
  }

  @Override
  public Mutation<StringState, String> combineWith(final Mutation<StringState, String> combine) {
    switch (combine.getType()) {
      case Delete:
        switch (type) {
          case Delete:
            if (position == combine.getPosition() + combine.length()) {
              return of(MutationType.Delete, combine.getPosition(), combine.getData() + data);
            }
            break;

        }

        break;
      case Insert:
        switch (type) {
          case Insert:
            if (position == combine.getPosition() + combine.length()) {
               return of(MutationType.Insert, combine.getPosition(), combine.getData() + data);
            }
            else if (position >= combine.getPosition() && position < combine.getPosition() + combine.length()) {
              final String frontTrim = combine.getData().substring(0, position - combine.getPosition());
              final String rearTrim = combine.getData().substring(position - combine.getPosition());
              return of(MutationType.Insert, combine.getPosition(), frontTrim + data + rearTrim);
            }
            break;
          case Delete:
            if (position >= combine.getPosition() && position < combine.getPosition() + combine.length()) {
              final String frontTrim = combine.getData().substring(0, position - combine.getPosition());
              final String rearTrim = combine.getData().substring(position - combine.getPosition() + length());

              return of(MutationType.Insert, combine.getPosition(), frontTrim.concat(rearTrim));
            }
            break;
        }
    }

    return null;

  }

  @Override
  public String toString() {
    if (getData() == null) {
      return type.getShortName() + "[" + getPosition() + "]";
    }
    else {
      return type.getShortName() + "[" + getPosition() + ",\"" + truncateString(getData()) + "\"]";
    }
  }

  private static String truncateString(String string) {
    if (string.length() > 8) {
      return string.substring(0, 8) + "...";
    }
    else {
      return string;
    }
  }
}

