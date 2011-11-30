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

package org.jboss.errai.marshalling.rebind.mappings.builtin;

import org.jboss.errai.marshalling.rebind.api.CustomMapping;
import org.jboss.errai.marshalling.rebind.api.model.MappingDefinition;
import org.jboss.errai.marshalling.rebind.api.model.impl.AccessorMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.ReadMapping;
import org.jboss.errai.marshalling.rebind.api.model.impl.SimpleConstructorMapping;

/**
 * @author Mike Brock
 */
@CustomMapping
public class ThrowableDefinition extends MappingDefinition {
  public ThrowableDefinition() {
    super(Throwable.class);

    SimpleConstructorMapping constructorMapping = new SimpleConstructorMapping(Throwable.class);
    constructorMapping.mapParmToIndex("message", 0, String.class);
    constructorMapping.mapParmToIndex("cause", 1, Throwable.class);

    setConstructorMapping(constructorMapping);

    addMemberMapping(new AccessorMapping(Throwable.class, "stackTrace", StackTraceElement[].class, "setStackTrace", "getStackTrace"));
    addMemberMapping(new ReadMapping(Throwable.class, "message", String.class, "getMessage"));
    addMemberMapping(new ReadMapping(Throwable.class, "cause", Throwable.class, "getCause"));
  }
}
