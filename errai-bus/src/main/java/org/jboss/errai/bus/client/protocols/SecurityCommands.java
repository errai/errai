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
 * The standard security protocol commands for use with ErraiBus.
 */
public enum SecurityCommands {
    /**
     * Requests what credentials are required in order to proceed.  Used as both a request and response command.
     */
    AuthenticationScheme,

    /**
     * Sent to attempt actual authorization with the remote server.
     */
    AuthRequest,

    /**
     * Sent to the client if the security authorization was successful.  The message will contain session context
     * and security principles information.
     */
    SecurityResponse,

    /**
     * Sent out as a challenge to a client when security constraints have been violated.  The message should contain
     * information on the required credentials to proceed.
     */
    SecurityChallenge,

    /**
     * Sent if the authentication has failed.
     */
    FailedAuth,

    /**
     * Sent to indicate that the authentication was successful.
     */
    SuccessfulAuth,

    /**
     * Send if authentication is not required.
     */
    AuthenticationNotRequired,

    /**
     * Terminates any existing session.
     */
    EndSession,

    /**
     * Sent back to the senders ReplyTo subject if the message sent by the client cannot be delivered to the intended
     * recipient.
     */
    MessageNotDelivered,

    /**
     * Send after successfull authentication and authorizazion
     */
    HandshakeComplete
}
