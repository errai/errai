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

package org.jboss.errai.enterprise.jaxrs.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * This service is used to test client exception mapping.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Path("/test/mapper")
public interface ClientExceptionMappingTestService {

  @GET
  public String getUsername(long userId);

  @GET
  public String getUsernameWithError(long userId) throws UserNotFoundException;

}
