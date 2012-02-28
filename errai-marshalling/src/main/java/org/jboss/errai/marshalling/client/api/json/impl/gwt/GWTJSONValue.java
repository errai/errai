/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.marshalling.client.api.json.impl.gwt;

import com.google.gwt.json.client.*;
import org.jboss.errai.marshalling.client.api.json.*;

/**
 * @author Mike Brock
 */
public class GWTJSONValue implements EJValue {
  final JSONValue value;

  final static EJNull NULL_INSTANCE = new EJNull() {
  };

  public GWTJSONValue(JSONValue value) {
    this.value = value;
  }

  @Override
  public EJArray isArray() {
    JSONArray array = value.isArray();
    if (array != null) {
      return new GWTJSONArray(array);
    }
    return null;
  }

  @Override
  public EJNumber isNumber() {
    JSONNumber num = value.isNumber();
    if (num != null) {
      return new GWTJSONNumber(num);
    }
    return null;
  }

  @Override
  public boolean isNull() {
    return value == null || value.isNull() != null;
  }

  @Override
  public EJObject isObject() {
    JSONObject obj = value.isObject();
    if (obj != null) {
      return new GWTJSONObject(obj);
    }
    return null;
  }

  @Override
  public EJBoolean isBoolean() {
    JSONBoolean bool = value.isBoolean();
    if (bool != null) {
      return new GWTJSONBoolean(bool);
    }
    return null;
  }

  @Override
  public EJString isString() {
    JSONString str = value.isString();
    if (str != null) {
      return new GWTJSONString(str);
    }
    return null;
  }

  @Override
  public Object getRawValue() {
    return value;
  }
}
