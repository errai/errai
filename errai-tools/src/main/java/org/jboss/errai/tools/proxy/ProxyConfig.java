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

package org.jboss.errai.tools.proxy;

import org.jboss.errai.marshalling.server.JSONDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 21, 2010
 */
public class ProxyConfig {
  private Map<String, Object> rootConfig;

  public final static String SERVICES = "services";
  public final static String ID = "id";
  public final static String URL = "url";
  public final static String CONTENT_TYPE = "contentType";
  public final static String PASSTHROUGH = "passthrough";

  protected ProxyConfig(Map<String, Object> rootConfig) {
    this.rootConfig = rootConfig;
  }

  public static ProxyConfig parse(String json) {
    ProxyConfig config = new ProxyConfig((Map<String, Object>) JSONDecoder.decode(json));
    return config;
  }

  public static ProxyConfig parse(InputStream in) {
    return parse(inputStreamToString(in));
  }

  public List<Map<String, Object>> getServices() {
    Map<String, Object> root = (Map<String, Object>) rootConfig.get("xhp");
    return (List) root.get(SERVICES);
  }

  private static String inputStreamToString(InputStream in) {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
      StringBuilder stringBuilder = new StringBuilder();
      String line = null;

      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }

      bufferedReader.close();
      return stringBuilder.toString();
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to parse input stream", e);
    }
  }
}
