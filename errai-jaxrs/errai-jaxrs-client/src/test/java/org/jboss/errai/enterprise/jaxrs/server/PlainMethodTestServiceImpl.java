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

import org.jboss.errai.enterprise.jaxrs.client.shared.PlainMethodTestService;

/**
 * Implementation of {@link PlainMethodTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PlainMethodTestServiceImpl implements PlainMethodTestService {

  @Override
  public String get() {
    return "get";
  }

  @Override
  public void getReturningVoid() {
    return;
  }

  @Override
  public String post() {
    return "post";
  }
  
  @Override
  public String postReturningNull() {
    return null;
  }

  @Override
  public String put() {
    return "put";
  }

  @Override
  public String delete() {
    return "delete";
  }
  
  @Override
  public String head() {
    return "head";
  }

  @Override
  public String getWithPathWithoutSlash() {
    return "getWithPathWithoutSlash";
  }
}
