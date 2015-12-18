/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Alternative;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@Alternative
@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

  public Map<String, Object> attributes = new HashMap<String, Object>();

  @Override
  public long getCreationTime() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public String getId() {
    return "M0CK_535510N_1D";
  }

  @Override
  public long getLastAccessedTime() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public ServletContext getServletContext() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setMaxInactiveInterval(int interval) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public int getMaxInactiveInterval() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public HttpSessionContext getSessionContext() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public Object getValue(String name) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return Collections.enumeration(attributes.keySet());
  }

  @Override
  public String[] getValueNames() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  @Override
  public void putValue(String name, Object value) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void removeAttribute(String name) {
    attributes.remove(name);
  }

  @Override
  public void removeValue(String name) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public void invalidate() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public boolean isNew() {
    throw new UnsupportedOperationException("Not implemented.");
  }

}
