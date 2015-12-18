/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.client.jaxrs;

import com.google.gwt.http.client.Response;

/**
 * A simple base class for exception mappers that expect a JSON payload
 * when unmarshalling errors.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractJSONClientExceptionMapper implements ClientExceptionMapper {
  
  /**
   * Constructor.
   */
  public AbstractJSONClientExceptionMapper() {
  }
  
  /**
   * Call this method to unmarshal the REST error response to a Bean of a 
   * particular type.
   * @param response
   * @param type
   */
  protected <T> T fromJSON(final Response response, final Class<T> type) {
    return MarshallingWrapper.fromJSON(response.getText(), type);
  }

}
