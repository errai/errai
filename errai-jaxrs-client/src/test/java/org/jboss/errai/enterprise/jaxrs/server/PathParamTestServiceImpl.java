/*
 * Copyright 2011 JBoss, a division of Red Hat Hat, Inc
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

package org.jboss.errai.enterprise.jaxrs.server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.jboss.errai.enterprise.jaxrs.client.shared.PathParamTestService;

/**
 * Implementation of {@link PathParamTestService} returning test data.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class PathParamTestServiceImpl implements PathParamTestService {

  @Override
  public long getWithPathParam(long id) {
    return id;
  }

  @Override
  public String getWithStringPathParam(String id) {
    try {
      return URLDecoder.decode(id, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      return e.toString();
    }
  }
  
  @Override
  public String getWithMultiplePathParams(long id1, long id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public String getWithReusedPathParam(long id1, long id2) {
    return "" + id1 + "/" + id2 + "/" + id1;
  }
  
  @Override
  public long postWithPathParam(long id) {
    return id;
  }

  @Override
  public long putWithPathParam(long id) {
    return id;
  }

  @Override
  public long deleteWithPathParam(long id) {
    return id;
  }
  
  @Override
  public void headWithPathParam(long id) {
  }
}
