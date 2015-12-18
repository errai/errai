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

package org.jboss.errai.enterprise.jaxrs.server;

import org.jboss.errai.enterprise.jaxrs.client.shared.HeaderParamTestService;

/**
 * Implementation of {@link HeaderParamTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class HeaderParamTestServiceImpl implements HeaderParamTestService {

  @Override
  public String getWithHeaderParam(String id) {
    return id;
  }

  @Override
  public String getWithMultipleHeaderParams(String id1, Float id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public String postWithHeaderParam(String entity, String id) {
    return entity + "/" + id;
  }

  @Override
  public String putWithHeaderParam(String id) {
    return id;
  }

  @Override
  public String deleteWithHeaderParam(String id) {
    return id;
  }

  @Override
  public void headWithHeaderParam(String header) {
    
  }
}
