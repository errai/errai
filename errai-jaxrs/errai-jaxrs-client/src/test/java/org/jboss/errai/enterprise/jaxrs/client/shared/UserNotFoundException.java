/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.enterprise.jaxrs.client.shared;

/**
 * Simple exception for testing.
 *
 * @author eric.wittmann@redhat.com
 */
public class UserNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -7684455400317960963L;

  /**
   * Constructor.
   */
  public UserNotFoundException() {
  }
  
  /**
   * Constructor.
   * @param userId
   */
  public UserNotFoundException(long userId) {
    super("User not found: " + userId);
  }
  
}
