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

package org.jboss.errai.otec.client;

import java.util.LinkedList;
import java.util.List;

import org.jboss.errai.otec.client.util.Md5Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StringState implements State<String> {
  private List<StateChangeListener> stateChangeListeners = new LinkedList<StateChangeListener>();
  public StringBuffer buffer;
  public String stateId = "<initial>";
  
  private static final Logger logger = LoggerFactory.getLogger(StringState.class);

  private StringState(final String buffer) {
    this.buffer = new StringBuffer(buffer);
  }

  private StringState(final StringBuffer buffer) {
    this.buffer = buffer;
  }

  public static StringState of(final String buffer) {
    return new StringState(buffer);
  }

  public void insert(final int pos, final char data) {
    if (pos == buffer.length()) {
      buffer.append(String.valueOf(data));
    }
    else {
      buffer.insert(pos, String.valueOf(data));
    }

    updateStateId();
    notifyStateChangeListeners(pos, 1);
  }

  public void insert(final int pos, final String data) {
    try {
      if (pos == buffer.length()) {
        buffer.append(data);
      }
      else {
        buffer.insert(pos, data);
      }

      updateStateId();
      notifyStateChangeListeners(pos, data.length());
    }
    catch (StringIndexOutOfBoundsException e) {
      System.out.println("********");
      System.out.println("FAILED TO INSERT: \"" + data + "\"");
      System.out.println("        POSITION: " + pos);
      System.out.println("      BUFFER LEN: " + buffer.length());

      e.printStackTrace(System.out);
      System.out.println("********");

      throw new OTException("could not update state", e);
    }
  }

  public void delete(final int pos) {
    buffer.delete(pos, pos + 1);
    updateStateId();
    notifyStateChangeListeners(pos, -1);
  }

  public void delete(final int pos, final int length) {
    try {
      buffer.delete(pos, pos + length);
      updateStateId();
      notifyStateChangeListeners(pos, -length);
    }
    catch (StringIndexOutOfBoundsException e) {
      System.out.println("********");
      System.out.println("FAILED TO DELETE: " + length);
      System.out.println("        POSITION: " + pos);
      System.out.println("      BUFFER LEN: " + buffer.length());
      throw new OTException("could not update state", e);
    }
  }

  private void updateStateId() {
    final String string = buffer.toString();
    stateId = createHashFor(string);
  }

  private void notifyStateChangeListeners(final int pos, final int offset) {
    for (final StateChangeListener listener : stateChangeListeners) {
      int cursorPos = listener.getCursorPos();
      if (cursorPos > pos) {
        cursorPos += offset;

        logger.debug("change pos: " + pos + "; offset: " + offset + "; newCursor: " + cursorPos);
      }

      if (cursorPos < 0) {
        cursorPos = 0;
      }

      if (cursorPos > length()) {
        cursorPos = length();
      }

      listener.onStateChange(cursorPos, buffer.toString());
    }
  }

  @Override
  public String get() {
    return buffer.toString();
  }

  @Override
  public State<String> snapshot() {
    return of(buffer.toString());
  }

  @SuppressWarnings("RedundantStringToString")
  @Override
  public void syncStateFrom(final State<String> fromState) {
    if (fromState instanceof StringState) {
      clear();
      buffer.append(fromState.get().toString());
      updateStateId();
    }
    else {
      throw new RuntimeException("cannot sync state with non-StringState");
    }
  }

  @Override
  public String getHash() {
    return stateId;
  }

  @Override
  public void clear() {
    buffer.delete(0, buffer.length());
  }

  @Override
  public State<String> getTransientState() {
    return new StringState(new StringBuffer(buffer));
  }

  @Override
  public void updateHash() {
    updateStateId();
  }

  @Override
  public int length() {
    return buffer.length();
  }

  @Override
  public ListenerRegistration addStateChangeListener(final StateChangeListener stateChangeListener) {
    stateChangeListeners.add(stateChangeListener);
    return new ListenerRegistration() {
      @Override
      public void remove() {
        stateChangeListeners.remove(stateChangeListener);
      }
    };
  }

  private static String createHashFor(final String string) {
    try {
      final Md5Digest digest = new Md5Digest();
      digest.update(string.getBytes("UTF-8"));
      return hashToHexString(digest.digest());
    }
    catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private static String hashToHexString(final byte[] hash) {
    final StringBuilder hexString = new StringBuilder(hash.length);
    for (final byte mdbyte : hash) {
      hexString.append(Integer.toHexString(0xFF & mdbyte));
    }
    return hexString.toString();
  }

  @Override
  public String toString() {
    return "\"" + buffer.toString() + "\"";
  }
}
