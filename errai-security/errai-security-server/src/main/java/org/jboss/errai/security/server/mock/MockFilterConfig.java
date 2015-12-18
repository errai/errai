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
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

@Alternative
public class MockFilterConfig implements FilterConfig {

  private final ServletContext owningContext;
  public Map<String, String> initParams = new HashMap<String, String>();

  public MockFilterConfig(ServletContext owningContext) {
    this.owningContext = owningContext;
  }

  @Override
  public String getFilterName() {
    throw new UnsupportedOperationException("Not implemented.");
  }

  @Override
  public ServletContext getServletContext() {
    return owningContext;
  }

  @Override
  public String getInitParameter(String name) {
    return initParams.get(name);
  }

  @Override
  public Enumeration<String> getInitParameterNames() {
    return Collections.enumeration(initParams.keySet());
  }
}
