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

import org.jboss.errai.otec.Position;

/**
 * @author Mike Brock
 */
public class IndexPosition implements Position {
  private final int position;

  private IndexPosition(int position) {
    this.position = position;
  }

  public static IndexPosition of(int position) {
    return new IndexPosition(position);
  }

  public int getPosition() {
    return this.position;
  }

  public String toString() {
    return String.valueOf(position);
  }
}
