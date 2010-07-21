/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.client.protocols;

/**
 * The parts comprising the standard security protocols.
 */
public enum SecurityParts {
    /**
     * The <tt>name</tt> of the principle being discussed.
     */
    Name,

    /**
     * The password for the principle.
     */
    Password,

    /**
     * A comma-seperated string of credentials required.
     */
    CredentialsRequired,

    /**
     * A comma-seperated string of roles.
     */
    Roles,

    /**
     * The actual credential objects associted with the session.
     */
    Credentials,

    /**
     * Message explaining the reason for a security rejectin.
     */
    RejectedMessage
}
