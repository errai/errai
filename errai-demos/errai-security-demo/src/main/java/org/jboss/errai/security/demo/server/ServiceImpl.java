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

package org.jboss.errai.security.demo.server;

import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.demo.client.shared.AdminService;
import org.jboss.errai.security.demo.client.shared.MessageService;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * The service implementation for {@link MessageService} and {@link AdminService}.
 */
@Service
@Dependent
public class ServiceImpl implements MessageService, AdminService {

  @Inject
  private AuthenticationService authenticationService;

  @Override
  public String hello() {
    final User user = authenticationService.getUser();
    String name = user.getProperty(FIRST_NAME) + " " + user.getProperty(LAST_NAME);
    return "Hello " + name + " how are you";
  }

  @Override
  public String ping() {
    return "pong";
  }
}
