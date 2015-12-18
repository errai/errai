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

package org.jboss.errai.security.demo.client.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;

/**
 * This is a secured Errai Bus RPC service. Any authenticated {@link User} can
 * access this service. Because the {@link RestrictedAccess} annotation is on
 * the type, any methods added to this type would also be secured. It is also
 * possible to annotate individual methods for finer-grained control, or to
 * annotate both the type and methods (in which case the roles will be combined
 * an all roles must be present for access to be granted).
 */
@Remote
@RestrictedAccess
public interface MessageService {
  String hello();
}
