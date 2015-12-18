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

package org.jboss.errai.enterprise.jaxrs.client.shared.interceptor;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestClientInterceptor;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User;
import org.jboss.errai.enterprise.jaxrs.client.shared.entity.User.Gender;

import com.google.gwt.http.client.RequestBuilder;

/**
 * Rest client interceptor for testing purposes. Manipulates the result returned from the remote endpoint.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class RestCallCustomTypeResultManipulatingInterceptor implements RestClientInterceptor {

  @Override
  public void aroundInvoke(final RestCallContext context) {
    final User user = new User(11l, "first", "last", 20, Gender.MALE,  null);
    final String userJackson = MarshallingWrapper.toJSON(user);
    
    String url = context.getRequestBuilder().getUrl();
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
    requestBuilder.setRequestData(userJackson);
    context.setRequestBuilder(requestBuilder);
    
    context.proceed(new RemoteCallback<User>() {
      @Override
      public void callback(User response) {
        String userAsJackson = response.getJacksonRep();
        User returnedUser = (User) MarshallingWrapper.fromJSON(userAsJackson, context.getReturnType());
        returnedUser.setFirstName("intercepted");
        context.setResult(returnedUser);
      }
    });
  }
}
