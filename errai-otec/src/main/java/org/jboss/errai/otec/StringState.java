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

package org.jboss.errai.otec;

/**
 * @author Mike Brock
 */
public class StringState implements State<String> {
  public StringBuilder buffer = new StringBuilder();

  public StringState(String buffer) {
    this.buffer = new StringBuilder(buffer);
  }

  public void replace(int pos, char data) {
    buffer.replace(pos, 0, String.valueOf(data));
  }

  public void insert(int pos, char data) {
    if (pos == buffer.length()) {
      buffer.append(String.valueOf(data));
    }
    else {
      buffer.insert(pos, String.valueOf(data));
    }
  }

  public void delete(int pos) {
    buffer.delete(pos, pos + 1);
  }

  @Override
  public String get() {
    return buffer.toString();
  }
}
