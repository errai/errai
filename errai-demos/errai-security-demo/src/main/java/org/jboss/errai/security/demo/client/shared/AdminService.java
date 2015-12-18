/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.demo.client.shared;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;

/**
 * This is a secured JAX-RS endpoint. The {@link AdminRoleProvider} will return the "admin"
 * {@link Role} at runtime, so this role is required to access the service. Because the
 * {@link RestrictedAccess} annotation is on the type, any methods added to this type would also be
 * secured. It is also possible to annotate individual methods for finer-grained control, or to
 * annotate both the type and methods (in which case the roles will be combined an all roles must be
 * present for access to be granted).
 */
@Path("/admin")
@RestrictedAccess(providers = { AdminRoleProvider.class })
public interface AdminService {

  @Path("/ping")
  @GET
  @Produces("application/json")
  String ping();
}
