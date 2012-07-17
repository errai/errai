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

package org.jboss.errai.enterprise.jaxrs.server;

import javax.ws.rs.core.PathSegment;

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
    return id;
  }
  
  @Override
  public String getWithMultiplePathParams(int id1, int id2) {
    return "" + id1 + "/" + id2;
  } 
  
  @Override
  public String getWithReusedPathParam(double id1, double id2) {
    return "" + id1 + "/" + id2 + "/" + id1;
  }
  
  @Override
  public Float postWithPathParam(Float id) {
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

  @Override
  public String getWithWithPathSegmentPathParam(PathSegment id) {
    return id.getPath();
  }
}
