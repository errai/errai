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

package org.jboss.errai.bus.client.api.builder;

/**
 * This interface, <tt>MessageBuildSubject</tt>, is a template for setting the subject of a message. This ensures
 * that the message is constructed properly
 */
public interface MessageBuildSubject<R> extends MessageBuild {

  /**
   * Sets the subject/receipent of the message, and returns a <tt>MessageBuildCommand</tt>, which needs to be
   * constructed following setting the subject
   *
   * @param subject - the subject of the message
   * @return an instance of <tt>MessageBuildCommand</tt>
   */
  public MessageBuildCommand<R> toSubject(String subject);

  /**
   * If this function is set, there is no need for a subject to be set for this message. Just move on...
   *
   * @return an instance of <tt>MessageBuildCommand</tt>
   */
  public MessageBuildCommand<R> subjectProvided();
}
