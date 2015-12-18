/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.util;

import java.util.Collection;

public interface Context {
  public void setAttribute(Enum<?> key, Object value);

  public void setAttribute(Class<?> typeIndexed, Object value);

  public void setAttribute(String param, Object value);

  public <T> T getAttribute(Class<T> type, Enum<?> key);

  public <T> T getAttribute(Class<T> type, Class<?> typeIndexed);

  public <T> T getAttribute(Class<T> type);

  public <T> T getAttribute(Class<T> type, String param);

  public boolean hasAttribute(String param);

  public Collection<String> getAttributeNames();

  public Object removeAttribute(Enum<?> key);

  public Object removeAttribute(Class<?> typeIndexed);

  public Object removeAttribute(String param);
}
