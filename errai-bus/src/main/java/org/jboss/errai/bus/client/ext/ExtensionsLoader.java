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

package org.jboss.errai.bus.client.ext;

import org.jboss.errai.bus.client.framework.MessageBus;

/**
 * This interface, <tt>ExtensionsLoader</tt>, is used internally during compile time to produce all the things
 * we want to initialize at runtime.
 */
public interface ExtensionsLoader {

    /**
     * Loads all the initialization extentions for the specified bus
     *
     * @param bus - the <tt>MessageBus</tt> to load the extensions for
     */
    public void initExtensions(MessageBus bus);
}