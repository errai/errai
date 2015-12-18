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

package org.jboss.errai.bus.server.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.errai.bus.client.api.Local;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;

/**
 * Parses and stores {@link Service} and {@link Command} meta-data for registering a
 * {@link MessageCallback}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public abstract class ServiceParser {

  protected boolean local;
  protected String svcName;
  protected Map<String, Method> commandPoints;

  /**
   * Get all (if any) {@link Command} endpoints for this service.
   * 
   * @return A map of command names to corresponding methods.
   */
  public Map<String, Method> getCommandPoints() {
    return commandPoints;
  }

  /**
   * @return True iff this service has any {@link Command} endpoints.
   */
  public boolean hasCommandPoints() {
    return getCommandPoints().size() != 0;
  }

  /**
   * @return The subject name of this service used for registering a {@link MessageCallback}.
   */
  public String getServiceName() {
    return svcName;
  }

  /**
   * @return True iff this is a {@link Local} service.
   */
  public boolean isLocal() {
    return local;
  }

  /**
   * @return The {@link Class} of the delegate instance for this service.<br/>
   * <br/>
   *         For a {@link Service} annotation on a type, this will be that type.<br/>
   *         For a {@link Service} annotation on a method, this will be the enclosing type.
   */
  public abstract Class<?> getDelegateClass();

  /**
   * @return True iff this is a type service with no command points, and this type is assignable to
   *         a {@link MessageCallback}.
   */
  public abstract boolean isCallback();

  public abstract MessageCallback getCallback(Object delegateInstance);

}
