/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.mock;

import org.jboss.errai.bus.server.util.SecureHashUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class MockHttpSession implements HttpSession{
  private final long creationTime;
  private final String sessionId;
  private final long lastAccessedTime;
  
  private final Map<String, Object> attributeMap;
  private final Map<String, Object> valueMap;
  
  private boolean valid;

  private MockHttpSession() {
    this.creationTime = System.currentTimeMillis();
    this.sessionId = SecureHashUtil.nextSecureHash("SHA-1");
    this.lastAccessedTime = System.currentTimeMillis();
    this.attributeMap = new HashMap<String, Object>();
    this.valueMap = new HashMap<String, Object>();
  }

  public static HttpSession createMock() {
    return new MockHttpSession();
  }

  @Override
  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public String getId() {
    return sessionId;
  }

  @Override
  public long getLastAccessedTime() {
    return lastAccessedTime;
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setMaxInactiveInterval(int i) {
  }

  @Override
  public int getMaxInactiveInterval() {
    return 0;
  }

  @Override
  public HttpSessionContext getSessionContext() {
    throw new UnsupportedOperationException();    
  }

  @Override
  public Object getAttribute(String s) {
    return attributeMap.get(s);
  }

  @Override
  public Object getValue(String s) {
    return valueMap.get(s);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return new Enumeration<String>() {
      Iterator<String> iter = attributeMap.keySet().iterator();

      @Override
      public boolean hasMoreElements() {
        return iter.hasNext();
      }

      @Override
      public String nextElement() {
        return iter.next();
      }
    };
  }

  @Override
  public String[] getValueNames() {
    return valueMap.keySet().toArray(new String[valueMap.size()]);
  }

  @Override
  public void setAttribute(String s, Object o) {
    attributeMap.put(s, o);
  }

  @Override
  public void putValue(String s, Object o) {
    valueMap.put(s, o);
  }

  @Override
  public void removeAttribute(String s) {
    attributeMap.remove(s);
  }

  @Override
  public void removeValue(String s) {
    valueMap.remove(s);
  }

  @Override
  public void invalidate() {
    valid = false;
  }

  @Override
  public boolean isNew() {
    return false;
  }
}
