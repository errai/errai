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

package org.jboss.errai.marshalling.client.api.json.impl.gwt;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.json.client.JSONObject;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJValue;

import java.util.Set;

/**
 * @author Mike Brock
 */
public class GWTJSONObject implements EJObject {
  final JSONObject obj;

  public GWTJSONObject(final JSONObject obj) {
    this.obj = obj;
  }

  @Override
  public EJValue get(final String name) {
    return new GWTJSONValue(obj.get(name));
  }
  
  @Override
  public EJValue getIfNotNull(final String name) {
    JSONValue v = obj.get(name);
    return v == null || v.isNull() != null ? null : new GWTJSONValue(v);
  }

  @Override
  public Set<String> keySet() {
    return obj.keySet();
  }

  @Override
  public boolean containsKey(final String name) {
    return obj.containsKey(name);
  }

  @Override
  public int size() {
    return obj.size();
  }
}
