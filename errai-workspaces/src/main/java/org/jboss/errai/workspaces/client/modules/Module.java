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
package org.jboss.errai.workspaces.client.modules;

/**
 * A module is a client side piece of code that intertacts
 * with the message bus. Modules may have UI components associated with
 * them, but they don't need to.<p/>
 * Initialization of a module (i.e. registering message callbacks)
 * should be done in the {@link #start()} method.
 */
public interface Module
{
  void start();
  void stop();
}
