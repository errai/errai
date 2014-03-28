/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.demo.server;

import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.*;

import javax.inject.Inject;

import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * @author edewit@redhat.com
 */
public class MessageServiceImpl implements MessageService {
  @Inject
  AuthenticationService authenticationService;

  @Override
  public String hello() {
    //User cannot be null because authentication is required for this method
    final User user = authenticationService.getUser();
    String name = user.getProperty(FIRST_NAME) + " " + user.getProperty(LAST_NAME);
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
